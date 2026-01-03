package org.cobalt.internal.loader

import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.client.TickEvent
import org.cobalt.api.notification.NotificationManager

object AddonScanner {

    private var tickCounter = 0
    private val scanInterval = 100 // Scan every 5 seconds (100 ticks)
    private var autoScanEnabled = true

    fun setAutoScan(enabled: Boolean) {
        autoScanEnabled = enabled
    }

    fun isAutoScanEnabled(): Boolean = autoScanEnabled

    @SubscribeEvent
    fun onTick(event: TickEvent.End) {
        if (!autoScanEnabled) return

        tickCounter++
        if (tickCounter >= scanInterval) {
            tickCounter = 0
            scanForNewAddons()
        }
    }

    private fun scanForNewAddons() {
        try {
            val newAddons = AddonLoader.scanForNewAddons()
            
            if (newAddons.isNotEmpty()) {
                AddonLoader.initializeAddons(newAddons)
                
                // Send notification for each new addon
                newAddons.forEach { (metadata, _) ->
                    if (metadata.mixins.isNotEmpty()) {
                        // Addon has mixins - requires restart
                        NotificationManager.sendNotification(
                            "Addon Loaded (Restart Required)",
                            "${metadata.name} v${metadata.version} has mixins. Please restart Minecraft to fully enable."
                        )
                    } else {
                        // Normal addon without mixins
                        NotificationManager.sendNotification(
                            "Addon Loaded",
                            "${metadata.name} v${metadata.version}"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            println("Error during addon scan: ${e.message}")
            e.printStackTrace()
        }
    }
}
