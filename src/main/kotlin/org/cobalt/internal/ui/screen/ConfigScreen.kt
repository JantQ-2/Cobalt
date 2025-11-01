package org.cobalt.internal.ui.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.cobalt.CoreMod.mc
import org.cobalt.api.util.helper.TickScheduler

internal object ConfigScreen : Screen(Text.empty()) {

  override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
    // Prevent rendering of the default screen background
  }

  fun openUI() {
    TickScheduler.schedule(1) {
      mc.setScreen(this@ConfigScreen)
    }
  }

}
