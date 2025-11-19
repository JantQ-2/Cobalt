package org.cobalt

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.cobalt.api.addon.Addon
import org.cobalt.api.command.CommandManager
import org.cobalt.api.event.EventBus
import org.cobalt.api.util.TickScheduler
import org.cobalt.internal.command.MainCommand
import org.cobalt.internal.feat.rpc.DiscordPresence
import org.cobalt.internal.helper.Config
import org.cobalt.internal.loader.AddonLoader

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
    ).forEach { EventBus.register(it) }

    Config.loadModulesConfig()
    println("Cobalt Mod Initialized")
  }

}
