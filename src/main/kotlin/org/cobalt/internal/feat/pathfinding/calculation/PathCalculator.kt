package org.cobalt.util.pathfinding.calculation

import net.minecraft.util.math.BlockPos
import org.cobalt.internal.feat.pathfinding.calculation.heuristic.EuclideanHeuristic
import org.cobalt.internal.feat.pathfinding.calculation.heuristic.IHeuristic
import org.cobalt.internal.feat.pathfinding.calculation.models.PathNode
import org.cobalt.internal.feat.pathfinding.calculation.movement.IMovementProvider
import org.cobalt.internal.feat.pathfinding.calculation.reconstruction.PathReconstructor
import org.cobalt.internal.feat.pathfinding.calculation.validation.IWalkabilityValidator
import org.cobalt.internal.feat.pathfinding.calculation.validation.StandardWalkabilityValidator
import java.util.*
import org.cobalt.CoreMod.mc
import org.cobalt.api.util.ChatUtils
import org.cobalt.internal.feat.pathfinding.calculation.movement.SkyblockMovementProvider

/**
 * A* pathfinding algorithm implementation
 *
 * Singleton object that provides pathfinding with configurable strategies
 * Default strategies can be overridden per call for flexibility
 */
internal object PathCalculator {

    private val defaultHeuristic: IHeuristic = EuclideanHeuristic()
    private val defaultWalkabilityValidator: IWalkabilityValidator = StandardWalkabilityValidator()
    private val pathReconstructor = PathReconstructor()

    /**
     * Finds a path from start to target using A* algorithm
     *
     * @param start Starting block position (where player's feet are)
     * @param target Target block position (where player's feet should be)
     * @param maxDistance Maximum search distance (prevents infinite loops)
     * @param heuristic Custom heuristic strategy (optional, uses default if not provided)
     * @param movementProvider Custom movement provider (optional, uses default if not provided)
     * @param walkabilityValidator Custom walkability validator (optional, uses default if not provided)
     * @return List of BlockPos representing the path, or empty list if no path found
     */
    fun findPath(
        start: BlockPos,
        target: BlockPos,
        maxDistance: Int = DEFAULT_MAX_DISTANCE,
        heuristic: IHeuristic = defaultHeuristic,
        movementProvider: IMovementProvider? = null,
        walkabilityValidator: IWalkabilityValidator = defaultWalkabilityValidator
    ): List<BlockPos> {
        val world = mc.world
        if (world == null) {
            ChatUtils.sendDebug("§c[Pathfinding] World is null! Cannot pathfind.")
            return emptyList()
        }

        if (!walkabilityValidator.isWalkable(world, target)) {
            ChatUtils.sendMessage("Pathfinding error!")
            ChatUtils.sendDebug("Pathfinding target is not walkable: $target")
            return emptyList()
        }

        // Create movement provider with world context if not provided
        val actualMovementProvider = movementProvider ?: SkyblockMovementProvider(world)

        // Check if start and target are within reasonable distance
        val squaredDistance = start.getSquaredDistance(target)
        val maxSquaredDistance = maxDistance * maxDistance

        if (squaredDistance > maxSquaredDistance) {
            return emptyList()
        }

        // Priority queue ordered by f-score (g + h)
        val openSet = PriorityQueue<PathNode>(compareBy { it.fScore })
        val closedSet = mutableSetOf<BlockPos>()
        val cameFrom = mutableMapOf<BlockPos, BlockPos>()

        // g-score: cost from start to this node
        val gScore = mutableMapOf<BlockPos, Double>()

        // Initialize starting node
        val startNode = PathNode(start, 0.0, heuristic.calculate(start, target))
        openSet.add(startNode)
        gScore[start] = 0.0

        var iterations = 0
        val maxIterations = maxDistance * maxDistance // Prevent infinite loops

        while (openSet.isNotEmpty() && iterations < maxIterations) {
            iterations++

            val current = openSet.poll()

            // Goal reached
            if (current.position == target) {
                return expandPathWithFallingPositions(pathReconstructor.reconstructPath(cameFrom, current.position))
            }

            closedSet.add(current.position)

            // Check all neighbors
            for (neighbor in actualMovementProvider.getNeighbors(current.position)) {
                if (closedSet.contains(neighbor)) continue

                // Check if the neighbor is walkable
                if (!walkabilityValidator.isWalkable(world, neighbor)) continue

                val movementCost = actualMovementProvider.getMovementCost(current.position, neighbor)
                val tentativeGScore = gScore[current.position]!! + movementCost

                // If this path to neighbor is better than any previous one
                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom[neighbor] = current.position
                    gScore[neighbor] = tentativeGScore
                    val fScore = tentativeGScore + heuristic.calculate(neighbor, target)

                    // Add or update in open set
                    openSet.removeIf { it.position == neighbor }
                    openSet.add(PathNode(neighbor, tentativeGScore, fScore))
                }
            }
        }

        // No path found
        return emptyList()
    }

    private fun expandPathWithFallingPositions(path: List<BlockPos>): List<BlockPos> {
        if (path.size < 2) return path

        val expandedPath = mutableListOf<BlockPos>()

        for (i in 0 until path.size - 1) {
            val current = path[i]
            val next = path[i + 1]

            // Always add the current position
            expandedPath.add(current)

            val dy = next.y - current.y

            // Check if this is a falling segment (more than 1 block down)
            if (dy < -1) {
                // Add all intermediate falling positions
                val fallDistance = kotlin.math.abs(dy)

                for (step in 1 until fallDistance) {
                    val intermediatePos = BlockPos(
                        next.x,  // Already at target x
                        current.y - step,  // Descending
                        next.z   // Already at target z
                    )
                    expandedPath.add(intermediatePos)
                }
            }
        }

        // Add the final position
        expandedPath.add(path.last())

        return expandedPath
    }

    private const val DEFAULT_MAX_DISTANCE = 100
}
