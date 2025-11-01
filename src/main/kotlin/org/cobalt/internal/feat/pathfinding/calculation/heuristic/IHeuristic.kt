package org.cobalt.internal.feat.pathfinding.calculation.heuristic

import net.minecraft.util.math.BlockPos

/**
 * Interface for heuristic distance calculations
 */
internal interface IHeuristic {
    /**
     * Calculates the estimated distance from one position to another
     */
    fun calculate(from: BlockPos, to: BlockPos): Double
}
