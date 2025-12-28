package org.cobalt.internal.ui

import net.minecraft.client.input.KeyInput

abstract class UIComponent(
  var x: Float,
  var y: Float,
  val width: Float,
  val height: Float
) {

  abstract fun render()

  open fun mouseClicked(button: Int): Boolean = false
  open fun mouseReleased(button: Int): Boolean = false
  open fun keyPressed(input: KeyInput): Boolean = false

  fun updateBounds(x: Float, y: Float): UIComponent {
    this.x = x
    this.y = y
    return this
  }

}
