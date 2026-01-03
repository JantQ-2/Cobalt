package org.cobalt.internal.command

import org.cobalt.api.command.Command
import org.cobalt.api.command.annotation.DefaultHandler
import org.cobalt.api.command.annotation.SubCommand
import org.cobalt.api.notification.NotificationManager
import org.cobalt.api.util.ChatUtils
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.loader.AddonScanner
import org.cobalt.internal.rotation.EasingType
import org.cobalt.internal.rotation.RotationExec
import org.cobalt.internal.rotation.strategy.TimedEaseStrategy
import org.cobalt.internal.ui.screen.UIConfig

internal object MainCommand : Command(
  name = "cobalt",
  aliases = arrayOf("cb")
) {

  @DefaultHandler
  fun main() {
    UIConfig.openUI()
  }

  @SubCommand
  fun rotate(yaw: Double, pitch: Double, duration: Int) {
    RotationExec.rotateTo(
      yaw.toFloat(),
      pitch.toFloat(),
      TimedEaseStrategy(
        yawEaseType = EasingType.EASE_OUT_EXPO,
        pitchEaseType = EasingType.EASE_OUT_EXPO,
        duration = duration.toLong()
      )
    )
  }

  @SubCommand
  fun notification(title: String, description: String) {
    NotificationManager.sendNotification(title, description)
  }

  @SubCommand
  fun reload() {
    ChatUtils.sendMessage("§7Scanning for new addons...")
    val newAddons = AddonLoader.scanForNewAddons()
    
    if (newAddons.isEmpty()) {
      ChatUtils.sendMessage("§7No new addons found")
    } else {
      AddonLoader.initializeAddons(newAddons)
      ChatUtils.sendMessage("§aLoaded §f${newAddons.size}§a new addon(s):")
      
      var requiresRestart = false
      newAddons.forEach { (metadata, _) ->
        val hasMixins = metadata.mixins.isNotEmpty()
        if (hasMixins) requiresRestart = true
        
        val mixinWarning = if (hasMixins) " §c[Mixins - Restart Required]" else ""
        ChatUtils.sendMessage("  §7- §f${metadata.name} §7v${metadata.version}$mixinWarning")
      }
      
      if (requiresRestart) {
        ChatUtils.sendMessage("§c⚠ Some addons have mixins. Please restart Minecraft to fully enable them.")
      }
    }
  }

  @SubCommand
  fun autoscan(enabled: String) {
    val enabledBool = when (enabled.lowercase()) {
      "true", "on", "enable", "enabled" -> true
      "false", "off", "disable", "disabled" -> false
      "status" -> {
        val status = if (AddonScanner.isAutoScanEnabled()) "§aenabled" else "§cdisabled"
        ChatUtils.sendMessage("§7Auto-scan is currently $status")
        return
      }
      else -> {
        ChatUtils.sendMessage("§cUsage: autoscan <true|false|status>")
        return
      }
    }
    
    AddonScanner.setAutoScan(enabledBool)
    val status = if (enabledBool) "§aenabled" else "§cdisabled"
    ChatUtils.sendMessage("§7Auto-scan $status")
  }

}
