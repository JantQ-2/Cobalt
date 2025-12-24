package org.cobalt.internal.ui.screen

import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.cobalt.Cobalt.mc
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.NvgEvent
import org.cobalt.api.util.TickScheduler
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.helper.Config
import org.cobalt.internal.ui.animation.EaseInOutAnimation
import org.cobalt.internal.ui.util.Constants

internal object UIScreen : Screen(Text.empty()) {

  /** Needed for opening animation */
  private val openAnim = EaseInOutAnimation(400)
  private var wasClosed = true

  init {
    EventBus.register(this)
  }

  @Suppress("unused")
  @SubscribeEvent
  fun onRender(event: NvgEvent) {
    if (mc.currentScreen != this)
      return

    val width = mc.window.width.toFloat()
    val height = mc.window.height.toFloat()

    NVGRenderer.beginFrame(width, height)

    if (openAnim.isAnimating()) {
      val scale = openAnim.get(0f, 1f)
      NVGRenderer.translate(width / 2, height / 2)
      NVGRenderer.scale(scale, scale)
      NVGRenderer.translate(-width / 2, -height / 2)
    }

    NVGRenderer.rect(
      (width / 2) - (Constants.BASE_WIDTH / 2),
      (height / 2) - (Constants.BASE_HEIGHT / 2),
      Constants.BASE_WIDTH,
      Constants.BASE_HEIGHT,
      Constants.COLOR_BACKGROUND.rgb,
      5F
    )

    NVGRenderer.endFrame()
  }

  override fun shouldPause(): Boolean {
    return false
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

  fun openUI() {
    TickScheduler.schedule(1) {
      mc.setScreen(this)
    }
  }

}
