package org.cobalt.internal.ui.component.impl

import java.awt.Color
import org.cobalt.api.module.Module
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.animation.ColorAnimation
import org.cobalt.internal.ui.component.Component
import org.cobalt.internal.ui.screen.ConfigScreen
import org.cobalt.internal.ui.util.Constants
import org.cobalt.internal.ui.util.Constants.MODULE_HEIGHT
import org.cobalt.internal.ui.util.Constants.MODULE_WIDTH
import org.cobalt.internal.ui.util.isHoveringOver

internal class ModuleComponent(
  private val module: Module,
) : Component() {

  private val colorAnim = ColorAnimation(250L)
  private var wasEnabled = module.isEnabled

  override fun draw(x: Float, y: Float) {
    super.draw(x, y)

    if (wasEnabled != module.isEnabled) {
      colorAnim.start()
      wasEnabled = module.isEnabled
    }

    val enabledBorder = Constants.COLOR_ACCENT
    val disabledBorder = Constants.COLOR_BORDER
    val border = colorAnim.get(disabledBorder, enabledBorder, !module.isEnabled).rgb

    val enabledBg = Color(52, 126, 178, 50)
    val disabledBg = Constants.COLOR_SURFACE
    val bg = colorAnim.get(disabledBg, enabledBg, !module.isEnabled).rgb

    val enabledText = Constants.COLOR_ACCENT
    val disabledText = Constants.COLOR_WHITE
    val text = colorAnim.get(disabledText, enabledText, !module.isEnabled).rgb

    NVGRenderer.rect(x, y, MODULE_WIDTH, MODULE_HEIGHT, bg, 4F)
    NVGRenderer.hollowRect(x, y, MODULE_WIDTH, MODULE_HEIGHT, 1F, border, 4F)

    NVGRenderer.text(
      module.name,
      x + 15F, y + (MODULE_HEIGHT / 2F) - 6F,
      12F, text,
    )
  }

  override fun onClick(button: Int): Boolean {
    if (!isHoveringOver(x, y, MODULE_WIDTH, MODULE_HEIGHT))
      return false

    when (button) {
      0 -> {
        module.isEnabled = !module.isEnabled
      }

      1 -> {
        ConfigScreen.setSelectedModule(this)
      }
    }

    return true
  }

  fun getModule(): Module {
    return module
  }

}
