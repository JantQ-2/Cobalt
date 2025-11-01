package org.cobalt.internal.feat.pathfinding.calculation.movement

import net.minecraft.block.SlabBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.abs
import net.minecraft.block.BlockState

/**
 * Standard 3D movement provider
 * Supports 8 horizontal directions, vertical movement, and diagonal climbing
 * Penalizes wall proximity and jumping
 */
internal class SkyblockMovementProvider(private val world: World? = null) : IMovementProvider {

    override fun getNeighbors(position: BlockPos): List<BlockPos> {
        val allNeighbors = super.getNeighbors(position)

        // Add fall-down landing positions (just the landing spots, not intermediates)
        val fallPositions = world?.let { w ->
            calculateFallPositions(w, position)
        } ?: emptyList()

        return world?.let { w ->
            (allNeighbors + fallPositions).filter { neighbor ->
                isValidMove(w, position, neighbor)
            }
        } ?: allNeighbors
    }

    override fun getMovementCost(from: BlockPos, to: BlockPos): Double {
        val dx = abs(from.x - to.x)
        val dy = to.y - from.y  // Keep sign for fall distance calculation
        val dz = abs(from.z - to.z)

        // Base movement cost
        var cost = when {
            // Vertical movement (jumping up)
            dy > 0 -> {
                // Check if "to" position has a slab below (climbing via slab)
                val isSlabClimb = world?.let { w ->
                    val blockBelow = w.getBlockState(to.down()).block
                    blockBelow is SlabBlock
                } ?: false

                if (isSlabClimb) {
                    SLAB_CLIMB_COST // Favor slab climbing
                } else {
                    JUMP_COST // Penalize jumping
                }
            }
            // Downward movement (falling)
            dy < 0 -> {
                val fallDistance = abs(dy)
                calculateFallCost(fallDistance)
            }
            // Diagonal horizontal movement
            dx > 0 && dz > 0 -> DIAGONAL_COST
            // Cardinal horizontal movement
            else -> CARDINAL_COST
        }

        // Add wall proximity penalty
        world?.let { w ->
            val penalty = calculateWallProximityPenalty(w, to)
            if (penalty > 0) {
                println("Position $to has wall penalty: $penalty")
            }
            cost += penalty
        }

        return cost
    }

    /**
     * Calculates the cost of falling based on distance
     */
    private fun calculateFallCost(fallDistance: Int): Double {
        return if (fallDistance <= FALL_DISTANCE_THRESHOLD) {
            fallDistance * FALL_COST_SHORT
        } else {
            // Cost for first 3 blocks + cost for remaining blocks
            (FALL_DISTANCE_THRESHOLD * FALL_COST_SHORT) +
                ((fallDistance - FALL_DISTANCE_THRESHOLD) * FALL_COST_LONG)
        }
    }

    /**
     * Calculates possible fall landing positions from current position
     * Returns only the final landing spots, not intermediate positions
     */
    private fun calculateFallPositions(world: World, position: BlockPos): List<BlockPos> {
        val fallPositions = mutableListOf<BlockPos>()

        // Check in all horizontal directions (cardinal + diagonal)
        val horizontalOffsets = listOf(
            BlockPos(1, 0, 0),   // East
            BlockPos(-1, 0, 0),  // West
            BlockPos(0, 0, 1),   // South
            BlockPos(0, 0, -1),  // North
            BlockPos(1, 0, 1),   // Southeast
            BlockPos(1, 0, -1),  // Northeast
            BlockPos(-1, 0, 1),  // Southwest
            BlockPos(-1, 0, -1)  // Northwest
        )

        for (offset in horizontalOffsets) {
            val horizontalPos = position.add(offset)

            // Find the landing position by checking downward
            val landingPos = findLandingPosition(world, horizontalPos, MAX_FALL_DISTANCE)
            if (landingPos != null && landingPos.y < position.y) {
                fallPositions.add(landingPos)
            }
        }

        // Also check straight down
        val straightDownLanding = findLandingPosition(world, position, MAX_FALL_DISTANCE)
        if (straightDownLanding != null && straightDownLanding.y < position.y) {
            fallPositions.add(straightDownLanding)
        }

        return fallPositions
    }

    /**
     * Finds where the player would land when falling from a position
     * Returns null if no landing spot within maxDistance or fall would be fatal
     */
    private fun findLandingPosition(world: World, startPos: BlockPos, maxDistance: Int): BlockPos? {
        var currentPos = startPos

        // Fall down until we hit a solid block or reach max distance
      (1..maxDistance).forEach { i ->
          val blockAtFeet = world.getBlockState(currentPos)
        val blockBelow = world.getBlockState(currentPos.down())

        // Check if current position is passable and block below is solid (landing spot)
        if (!isBlockSolid(world, currentPos, blockAtFeet) &&
          isBlockSolid(world, currentPos.down(), blockBelow)) {
          // Check if there's head clearance
          val blockAtHead = world.getBlockState(currentPos.up())
          if (!isBlockSolid(world, currentPos.up(), blockAtHead)) {
            return currentPos
          }
        }

        currentPos = currentPos.down()
      }

        return null
    }

    /**
     * Checks if a move from one position to another is physically valid
     */
    private fun isValidMove(world: World, from: BlockPos, to: BlockPos): Boolean {
        val dy = to.y - from.y

        // If moving up, check if the vertical jump is possible
        if (dy > 0) {
            val blockBelowFrom = world.getBlockState(from.down()).block
            val blockBelowTo = world.getBlockState(to.down()).block

            val fromIsOnSlab = blockBelowFrom is SlabBlock
            val toIsOnSlab = blockBelowTo is SlabBlock

            // Can't jump 1.5 blocks (full block to full block)
            if (!fromIsOnSlab && !toIsOnSlab && dy == 1) {
                return false
            }
        }

        // If falling more than 1 block, only allow if it's a calculated fall position
        // (single block down is already in default neighbors)
        if (dy < -1) {
            val fallDistance = abs(dy)
            if (fallDistance > MAX_FALL_DISTANCE) {
                return false
            }
            // Additional validation: verify all intermediate positions are passable
            return isPathClearForFall(world, from, to)
        }

        // Check if destination has enough space for player (2 blocks tall)
        val blockAtFeet = world.getBlockState(to)
        val blockAtHead = world.getBlockState(to.up())

        // Both feet and head positions must be passable
        if (isBlockSolid(world, to, blockAtFeet) || isBlockSolid(world, to.up(), blockAtHead)) {
            return false
        }

        return true
    }

    /**
     * Checks if the path is clear for falling from one position to another
     */
    private fun isPathClearForFall(world: World, from: BlockPos, to: BlockPos): Boolean {
        val fallDistance = from.y - to.y

        // Check all intermediate positions
        for (i in 1 until fallDistance) {
            val intermediatePos = BlockPos(to.x, from.y - i, to.z)
            val blockAtFeet = world.getBlockState(intermediatePos)
            val blockAtHead = world.getBlockState(intermediatePos.up())

            // Both positions must be passable
            if (isBlockSolid(world, intermediatePos, blockAtFeet) ||
                isBlockSolid(world, intermediatePos.up(), blockAtHead)) {
                return false
            }
        }

        return true
    }

    /**
     * Calculates a penalty based on proximity to walls
     * Checks a 2-block radius around the position
     */
    private fun calculateWallProximityPenalty(world: World, position: BlockPos): Double {
        var wallCount = 0
        var immediateWallCount = 0

        // Check immediate neighbors (1 block away)
        val immediateOffsets = listOf(
            BlockPos(1, 0, 0),
            BlockPos(-1, 0, 0),
            BlockPos(0, 0, 1),
            BlockPos(0, 0, -1)
        )

        for (offset in immediateOffsets) {
            val checkPos = position.add(offset)
            if (isWall(world, checkPos)) {
                immediateWallCount++
            }
        }

        // Check 2 blocks away (diagonal and extended cardinal)
        val extendedOffsets = listOf(
            BlockPos(2, 0, 0),
            BlockPos(-2, 0, 0),
            BlockPos(0, 0, 2),
            BlockPos(0, 0, -2),
            BlockPos(1, 0, 1),
            BlockPos(1, 0, -1),
            BlockPos(-1, 0, 1),
            BlockPos(-1, 0, -1)
        )

        for (offset in extendedOffsets) {
            val checkPos = position.add(offset)
            if (isWall(world, checkPos)) {
                wallCount++
            }
        }

        // Calculate penalty
        // Immediate walls (1 block) have higher penalty
        val immediatePenalty = immediateWallCount * WALL_PENALTY_IMMEDIATE
        // Walls 2 blocks away have lower penalty
        val extendedPenalty = wallCount * WALL_PENALTY_EXTENDED

        return immediatePenalty + extendedPenalty
    }

    /**
     * Checks if a position has a wall (solid block at body height)
     */
    private fun isWall(world: World, position: BlockPos): Boolean {
        // Check both feet level and head level (player is 2 blocks tall)
        val blockAtFeet = world.getBlockState(position)
        val blockAtHead = world.getBlockState(position.up())

        return isBlockSolid(world, position, blockAtFeet) ||
            isBlockSolid(world, position.up(), blockAtHead)
    }

    private fun isBlockSolid(world: World, pos: BlockPos, state: BlockState): Boolean {
        return (state.isSolidBlock(world, pos) || state.blocksMovement()) && !state.isLiquid
    }

    companion object {
        private const val CARDINAL_COST = 1.0
        private const val DIAGONAL_COST = 1.414
        private const val JUMP_COST = 5.0 // High cost to discourage jumping
        private const val SLAB_CLIMB_COST = 1.3 // Lower cost for climbing via slabs

        // Fall/drop settings
        private const val FALL_DISTANCE_THRESHOLD = 3 // Blocks before fall cost changes
        private const val FALL_COST_SHORT = 1.0 // Cost per block for falls <= threshold
        private const val FALL_COST_LONG = 0.8 // Cost per block for falls > threshold
        private const val MAX_FALL_DISTANCE = 20 // Maximum safe fall distance

        // Wall proximity penalties
        private const val WALL_PENALTY_IMMEDIATE = 10.0 // Penalty per wall 1 block away
        private const val WALL_PENALTY_EXTENDED = 0.5  // Penalty per wall 2 blocks away
    }
}
