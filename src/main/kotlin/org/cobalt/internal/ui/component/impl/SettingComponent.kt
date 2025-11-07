package org.cobalt.internal.ui.component.impl

import org.cobalt.api.module.setting.Setting
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.component.Component
import org.cobalt.internal.ui.util.Constants

internal class SettingComponent(
  private val setting: Setting<*>,
) : Component() {

  override fun draw(x: Float, y: Float) {
    super.draw(x, y)

    NVGRenderer.rect(
      x, y,
      Constants.SETTING_WIDTH, Constants.SETTING_HEIGHT,
      Constants.COLOR_SURFACE.rgb, 4F,
    )

    NVGRenderer.hollowRect(
      x, y,
      Constants.SETTING_WIDTH, Constants.SETTING_HEIGHT,
      1F, Constants.COLOR_BORDER.rgb, 4F,
    )
  }

}
