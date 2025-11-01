package org.cobalt.internal.feat.pathfinding.calculation.movement

import net.minecraft.util.math.BlockPos

/**
 * Interface for providing valid movement options from a position
 */
internal interface IMovementProvider {
    /**
     * Gets all possible neighbor positions from the given position
     */
    fun getNeighbors(position: BlockPos): List<BlockPos> {
        return listOf(
            // Horizontal neighbors (4 cardinal + 4 diagonal)
            position.add(1, 0, 0),   // East
            position.add(-1, 0, 0),  // West
            position.add(0, 0, 1),   // South
            position.add(0, 0, -1),  // North
            position.add(1, 0, 1),   // Southeast
            position.add(1, 0, -1),  // Northeast
            position.add(-1, 0, 1),  // Southwest
            position.add(-1, 0, -1), // Northwest

            // Vertical neighbors
            position.add(0, 1, 0),   // Up
            position.add(0, -1, 0),  // Down

            // Diagonal vertical (for climbing/descending slopes)
            position.add(1, 1, 0),
            position.add(-1, 1, 0),
            position.add(0, 1, 1),
            position.add(0, 1, -1),
            position.add(1, -1, 0),
            position.add(-1, -1, 0),
            position.add(0, -1, 1),
            position.add(0, -1, -1)
        )
    }

    /**
     * Calculates the cost of moving between two adjacent positions
     */
    fun getMovementCost(from: BlockPos, to: BlockPos): Double
}
