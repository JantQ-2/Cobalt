package org.cobalt

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.cobalt.api.addon.Addon
import org.cobalt.api.command.CommandManager
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.WorldRenderEvent
import org.cobalt.api.pathfinder.IPathExec
import org.cobalt.api.util.TickScheduler
import org.cobalt.api.util.rotation.IRotationExec
import org.cobalt.internal.command.MainCommand
import org.cobalt.internal.helper.Config
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.modules.DiscordPresence
import org.cobalt.internal.pathfinder.PathExec
import org.cobalt.internal.rotation.RotationExec

object Cobalt : ClientModInitializer {

  const val MOD_NAME = "Cobalt"
  const val VERSION = "1.0.0"
  const val MC_VERSION = "1.21.10"

  val mc: MinecraftClient
    get() = MinecraftClient.getInstance()

  @Suppress("UNUSED_EXPRESSION")
  override fun onInitializeClient() {
    AddonLoader.getAddons()
      .map { it.second }
      .forEach(Addon::onLoad)

    CommandManager.register(MainCommand)
    CommandManager.dispatchAll()

    listOf(
      TickScheduler,
      DiscordPresence,
      MainCommand
    ).forEach { EventBus.register(it) }

    Config.loadModulesConfig()
    EventBus.register(this)
    println("Cobalt Mod Initialized")
  }

  @JvmStatic
  private var rotationExec: IRotationExec = RotationExec

  @JvmStatic
  private var pathExec: IPathExec = PathExec

  @SubscribeEvent
  fun onWorldRenderLast(event: WorldRenderEvent.Last) {
    mc.player?.let {
      rotationExec.onRotate(it)
    }
  }

  @JvmStatic
  fun getPathExec(): IPathExec {
    return pathExec
  }

  @JvmStatic
  fun setRotationExec(pathExec: IPathExec) {
    this.pathExec = pathExec
  }

  @JvmStatic
  fun getRotationExec(): IRotationExec {
    return rotationExec
  }

  @JvmStatic
  fun setRotationExec(rotationExec: IRotationExec) {
    this.rotationExec = rotationExec
  }

}
