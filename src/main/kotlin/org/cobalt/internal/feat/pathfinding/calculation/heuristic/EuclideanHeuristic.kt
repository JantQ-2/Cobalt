package org.cobalt.internal.feat.pathfinding.calculation.heuristic

import net.minecraft.util.math.BlockPos
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Euclidean distance heuristic for A* pathfinding
 * Calculates straight-line distance between two points
 */
internal class EuclideanHeuristic : IHeuristic {
    override fun calculate(from: BlockPos, to: BlockPos): Double {
        val dx = abs(from.x - to.x).toDouble()
        val dy = abs(from.y - to.y).toDouble()
        val dz = abs(from.z - to.z).toDouble()
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
