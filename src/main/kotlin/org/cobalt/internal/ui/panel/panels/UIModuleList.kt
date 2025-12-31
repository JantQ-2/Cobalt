package org.cobalt.internal.ui.panel.panels

import java.awt.Color
import org.cobalt.api.addon.Addon
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.api.util.ui.helper.Gradient
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.components.UIBackButton
import org.cobalt.internal.ui.components.UIModule
import org.cobalt.internal.ui.components.UITopbar
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.util.UIGridLayout
import org.cobalt.internal.ui.util.UIScrollHandler
import org.cobalt.internal.ui.util.isHoveringOver

internal class UIModuleList(
  private val metadata: AddonLoader.AddonMetadata,
  addon: Addon,
) : UIPanel(
  x = 0F,
  y = 0F,
  width = 890F,
  height = 600F
) {

  private val topBar = UITopbar("Modules")
  private val backButton = UIBackButton()

  private val modules = addon.getModules()
    .withIndex()
    .flatMap { (index, module) ->
      if (index == 0) {
        List(1) { UIModule(module, this, true) }
      } else {
        List(10) { UIModule(module, this, false) }
      }
    }

  private val modulesLayout = UIGridLayout(
    columns = 1,
    itemWidth = 182.5F,
    itemHeight = 40F,
    gap = 5F
  )

  private val modulesScroll = UIScrollHandler()
  private var module = modules.first()

  init {
    components.addAll(modules)
    components.add(backButton)
    components.add(topBar)
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(18, 18, 18).rgb, 10F)
    NVGRenderer.line(
      x + width / 4F,
      y + topBar.height / 2 + height * 1F / 8F,
      x + width / 4F,
      y + topBar.height / 2 + height * 7F / 8F,
      1F, Color(42, 42, 42).rgb
    )

    topBar
      .updateBounds(x, y)
      .render()

    backButton
      .updateBounds(x + 20F, y + topBar.height + 20F)
      .render()

    NVGRenderer.text(
      metadata.name,
      x + backButton.width + 35F,
      y + topBar.height + 27.5F,
      15F, Color(230, 230, 230).rgb
    )

    val startY = y + topBar.height + backButton.height + 40F
    val visibleHeight = height - (topBar.height + backButton.height + 40F)

    modulesScroll.setMaxScroll(modulesLayout.contentHeight(modules.size) + 20F, visibleHeight)
    NVGRenderer.pushScissor(x, startY, width / 4F, visibleHeight)

    val scrollOffset = modulesScroll.getOffset()
    modulesLayout.layout(x + 20F, startY - scrollOffset, modules)
    modules.forEach(UIComponent::render)

    NVGRenderer.popScissor()
  }

  override fun mouseScrolled(horizontalAmount: Double, verticalAmount: Double): Boolean {
    if (isHoveringOver(x, y, width / 4F, height)) {
      modulesScroll.handleScroll(verticalAmount)
      return true
    }

    return false
  }

  fun setModule(module: UIModule) {
    modules.forEach {
      when {
        it == module -> module.setSelected()
        else -> it.setSelected(false)
      }
    }

    this.module = module
  }

}
