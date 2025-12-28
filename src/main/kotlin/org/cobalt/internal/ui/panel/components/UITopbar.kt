package org.cobalt.internal.ui.panel.components

import java.awt.Color
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent

class UITopbar(
  private var title: String,
) : UIComponent(
  x = 0F,
  y = 0F,
  width = 870F,
  height = 60F,
) {

  private val searchBar = UISearchBar()

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(24, 24, 24).rgb, 10F)
    NVGRenderer.hollowRect(x, y, width, height, 2F, Color(79, 140, 255, 100).rgb, 10F)
    NVGRenderer.text(title, x + 20F, y + 20F, 20F, Color(230, 230, 230).rgb)

    searchBar
      .updateBounds(x, y)
      .render()
  }

  private class UISearchBar : UIComponent(
    x = 0F,
    y = 0F,
    width = 0F,
    height = 0F,
  ) {

    override fun render() {

    }

  }

}
