package org.cobalt.internal.ui.panel

import net.minecraft.client.input.KeyInput
import org.cobalt.internal.ui.UIComponent

abstract class UIPanel(
  x: Float,
  y: Float,
  width: Float,
  height: Float
) : UIComponent(x, y, width, height) {

  val components = mutableListOf<UIComponent>()

  override fun mouseClicked(button: Int) =
    components.any { it.mouseClicked(button) }

  override fun mouseReleased(button: Int) =
    components.any { it.mouseReleased(button) }

  override fun keyPressed(input: KeyInput) =
    components.any { it.keyPressed(input) }

}
