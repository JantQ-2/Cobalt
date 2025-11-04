package org.cobalt.internal.ui.component.impl

import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.component.Component
import org.cobalt.internal.ui.util.Constants

internal object SearchbarComponent : Component() {

  override fun draw(x: Float, y: Float) {
    super.draw(x, y)

    NVGRenderer.hollowRect(
      x, y,
      Constants.SEARCHBAR_WIDTH, Constants.SEARCHBAR_HEIGHT,
      1F, Constants.COLOR_BORDER.rgb, 4F
    )

    NVGRenderer.rect(
      x, y,
      Constants.SEARCHBAR_WIDTH, Constants.SEARCHBAR_HEIGHT,
      Constants.COLOR_SURFACE.rgb, 4F
    )

    NVGRenderer.image(
      searchIcon,
      x + 15F, y + (Constants.SEARCHBAR_HEIGHT / 2) - 7F,
      14F, 14F,
      colorMask = Constants.COLOR_BORDER.rgb
    )

    // TODO: Implement typing searchbar component
  }

  val searchIcon = NVGRenderer.createImage("/assets/cobalt/icons/search.svg")

}
