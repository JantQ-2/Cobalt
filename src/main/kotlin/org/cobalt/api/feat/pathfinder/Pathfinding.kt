package org.cobalt.api.feat.pathfinder

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.BlockPos

interface Pathfinding {
  val name: String

  fun findPath (goal: BlockPos, player: ClientPlayerEntity): List<BlockPos>
}
