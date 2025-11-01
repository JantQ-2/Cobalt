package org.cobalt.internal.feat.pathfinding.calculation.validation

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Interface for validating whether a position is walkable
 */
internal interface IWalkabilityValidator {
    /**
     * Checks if a position is walkable for a player
     * @param world The world to check in
     * @param position The block position where the player's feet would be
     * @return true if the position is walkable, false otherwise
     */
    fun isWalkable(world: World, position: BlockPos): Boolean
}
