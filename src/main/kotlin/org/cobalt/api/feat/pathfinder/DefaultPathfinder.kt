package org.cobalt.api.feat.pathfinder

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.client.gui.screen.ChatScreen
import org.cobalt.Cobalt.mc
import org.cobalt.api.event.EventBus
import org.cobalt.api.util.ChatUtils
import org.cobalt.util.pathfinding.calculation.PathCalculator

object DefaultPathfinder : Pathfinding {
  init {
    EventBus.register(this)
  }

  override val name: String
    get() = "Default"

  override fun findPath(goal: BlockPos, player: ClientPlayerEntity): List<BlockPos> {
    if (mc.world == null || mc.player == null || (mc.currentScreen != null && mc.currentScreen !is ChatScreen)) {
      ChatUtils.sendDebug("Attempting to start Pathfinder, but can't. World: ${mc.world.toString()}, Player: ${mc.player.toString()}, currentScreen: ${mc.currentScreen.toString()}")
      throw IllegalStateException("Attempting to start pathfinder, but can't!")
    }

    val startSearchTime = System.currentTimeMillis()
    val path = PathCalculator.findPath(mc.player!!.blockPos, goal, 256)
    ChatUtils.sendDebug("Found path in ${System.currentTimeMillis() - startSearchTime}ms")
    return path
  }
}
