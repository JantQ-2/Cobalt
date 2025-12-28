package org.cobalt.internal.ui.panel.panels

import java.awt.Color
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.panel.components.UITopbar

class UIModules : UIPanel(
  x = 0F,
  y = 0F,
  width = 890F,
  height = 600F
) {

  val topBar = UITopbar("Modules")

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(18, 18, 18).rgb, 10F)

    topBar
      .updateBounds(x + (width / 2F) - (topBar.width / 2F), y + 10F)
      .render()
  }

}
