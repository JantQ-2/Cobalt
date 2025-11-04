package org.cobalt.internal.ui.component.impl

import org.cobalt.api.module.Category
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.animation.ColorAnimation
import org.cobalt.internal.ui.component.Component
import org.cobalt.internal.ui.screen.ConfigScreen
import org.cobalt.internal.ui.util.Constants
import org.cobalt.internal.ui.util.isHoveringOver

internal class CategoryComponent(
  private val category: Category,
) : Component() {

  private val colorAnim = ColorAnimation(250L)
  private var wasSelected = false

  fun draw(x: Float, y: Float, selectedCategory: Category?) {
    super.draw(x, y)

    val isSelected = category == selectedCategory

    if (wasSelected != isSelected) {
      colorAnim.start()
      wasSelected = isSelected
    }

    val selectedColor = Constants.COLOR_ACCENT
    val unselectedColor = Constants.COLOR_BORDER
    val colorMask = colorAnim.get(unselectedColor, selectedColor, !isSelected).rgb

    NVGRenderer.image(
      image,
      x, y,
      20F, 20F,
      colorMask = colorMask
    )
  }

  override fun onClick(button: Int): Boolean {
    if (!isHoveringOver(x, y, 20F, 20F))
      return false

    ConfigScreen.setSelectedCategory(category)
    return true
  }

  val image = NVGRenderer.createImage(category.svg)

}
