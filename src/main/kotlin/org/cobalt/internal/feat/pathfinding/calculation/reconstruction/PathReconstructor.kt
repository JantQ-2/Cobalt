package org.cobalt.internal.feat.pathfinding.calculation.reconstruction

import net.minecraft.util.math.BlockPos

/**
 * Handles reconstruction of paths from the A* search results
 */
internal class PathReconstructor {

    /**
     * Reconstructs the path by backtracking through the cameFrom map
     * @param cameFrom Map of position -> previous position in the path
     * @param target The target position (end of path)
     * @return List of positions from start to target
     */
    fun reconstructPath(cameFrom: Map<BlockPos, BlockPos>, target: BlockPos): List<BlockPos> {
        val path = mutableListOf(target)
        var currentPos = target

        while (cameFrom.containsKey(currentPos)) {
            currentPos = cameFrom[currentPos]!!
            path.add(0, currentPos) // Add to beginning
        }

        return path
    }
}
