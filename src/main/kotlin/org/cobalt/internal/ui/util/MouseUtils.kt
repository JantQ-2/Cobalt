package org.cobalt.internal.ui.util

import org.cobalt.CoreMod.mc

inline val mouseX: Double
  get() = mc.mouse.x

inline val mouseY: Double
  get() = mc.mouse.y

fun isHoveringOver(x: Double, y: Double, width: Double, height: Double): Boolean =
    mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
