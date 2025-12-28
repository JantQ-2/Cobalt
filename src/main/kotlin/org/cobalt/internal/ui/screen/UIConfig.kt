package org.cobalt.internal.ui.screen

import net.minecraft.client.gui.Click
import net.minecraft.client.input.KeyInput
import org.cobalt.Cobalt.mc
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.NvgEvent
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.helper.Config
import org.cobalt.internal.ui.UIScreen
import org.cobalt.internal.ui.animation.EaseInOutAnimation
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.panel.panels.UIModules
import org.cobalt.internal.ui.panel.panels.UISidebar

object UIConfig : UIScreen() {

  /** Needed for opening animation */
  private val openAnim = EaseInOutAnimation(400)
  private var wasClosed = true

  /** UI Panels */
  private val sidebar = UISidebar()
  private var body: UIPanel = UIModules()

  init {
    EventBus.register(this)
  }

  @Suppress("unused")
  @SubscribeEvent
  fun onRender(event: NvgEvent) {
    if (mc.currentScreen != this)
      return

    val window = mc.window
    val width = window.width.toFloat()
    val height = window.height.toFloat()

    NVGRenderer.beginFrame(width, height)

    if (openAnim.isAnimating()) {
      val scale = openAnim.get(0f, 1f)
      val cx = width / 2f
      val cy = height / 2f

      NVGRenderer.translate(cx, cy)
      NVGRenderer.scale(scale, scale)
      NVGRenderer.translate(-cx, -cy)
    }

    val originX = width / 2f - 480f
    val originY = height / 2f - 300f

    sidebar
      .updateBounds(originX, originY)
      .render()

    body
      .updateBounds(originX + 80f, originY)
      .render()

    NVGRenderer.endFrame()
  }

  override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
    return body.mouseClicked(click.button()) ||
      sidebar.mouseClicked(click.button()) ||
      super.mouseClicked(click, doubled)
  }

  override fun mouseReleased(click: Click): Boolean {
    return body.mouseReleased(click.button()) ||
      sidebar.mouseReleased(click.button()) ||
      super.mouseReleased(click)
  }

  override fun keyPressed(input: KeyInput): Boolean {
    return body.keyPressed(input) ||
      sidebar.keyPressed(input) ||
      super.keyPressed(input)
  }

  override fun init() {
    if (wasClosed) {
      openAnim.start()
      wasClosed = false
    }

    super.init()
  }

  override fun close() {
    Config.saveModulesConfig()
    wasClosed = true
    super.close()
  }

}
