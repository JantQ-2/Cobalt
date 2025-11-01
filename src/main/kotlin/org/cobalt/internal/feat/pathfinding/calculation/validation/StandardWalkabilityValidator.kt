package org.cobalt.internal.feat.pathfinding.calculation.validation

import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.block.enums.SlabType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Standard walkability validator for Minecraft
 * Checks that the floor is solid and the body/head spaces are passable
 */
internal class StandardWalkabilityValidator : IWalkabilityValidator {

    override fun isWalkable(world: World, position: BlockPos): Boolean {
        // The block BELOW position is where the player stands ON (must be solid)
        val blockStateBelow = world.getBlockState(position.down())
        val blockBelow = blockStateBelow.block

        // The position itself is where the player's feet are (must be passable)
        val blockAtFeet = world.getBlockState(position)

        // The space above is where the player's head is (must be passable)
        val blockAtHead = world.getBlockState(position.up())

        // Floor (block below) must be solid or a walkable partial block
        val hasValidFloor = when {
            blockStateBelow.isSolidBlock(world, position.down()) -> true
            blockStateBelow.isLiquid -> true
            blockBelow is SlabBlock -> {
                // Bottom slabs and double slabs are walkable
                val slabType = blockStateBelow.get(SlabBlock.TYPE)
                slabType == SlabType.BOTTOM || slabType == SlabType.DOUBLE
            }
            blockBelow is StairsBlock -> true // Stairs are walkable
            else -> false
        }

        if (!hasValidFloor) {
            return false
        }

        // Feet space must be passable (air or non-solid)
        // Top slabs at feet level should be passable (player can walk through the air above them)
        if (!blockAtFeet.isAir && blockAtFeet.blocksMovement()) {
            // Allow top slabs at feet level
            if (blockAtFeet.block is SlabBlock) {
                val slabType = blockAtFeet.get(SlabBlock.TYPE)
                if (slabType == SlabType.TOP) {
                    // Top slab at feet level is fine, continue checking
                } else {
                    return false // Bottom or double slab blocks movement
                }
            } else {
                return false
            }
        }

        // Head space must be passable
        if (!blockAtHead.isAir && blockAtHead.blocksMovement()) {
            return false
        }

        return true
    }
}
