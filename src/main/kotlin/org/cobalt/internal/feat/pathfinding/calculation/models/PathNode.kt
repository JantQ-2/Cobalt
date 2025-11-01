package org.cobalt.internal.feat.pathfinding.calculation.models

import net.minecraft.util.math.BlockPos

/**
 * Represents a node in the A* pathfinding algorithm
 * @param position The block position in the world
 * @param gScore Cost from start to this node
 * @param fScore Total estimated cost (g + h)
 */
internal data class PathNode(
    val position: BlockPos,
    val gScore: Double,
    val fScore: Double
)
