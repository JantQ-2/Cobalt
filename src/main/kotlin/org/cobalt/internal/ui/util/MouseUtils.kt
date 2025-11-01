package org.cobalt.internal.ui.util

import org.cobalt.CoreMod.mc

internal inline val mouseX: Double
  get() = mc.mouse.x

internal inline val mouseY: Double
  get() = mc.mouse.y

internal fun isHoveringOver(x: Double, y: Double, width: Double, height: Double): Boolean =
    mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
