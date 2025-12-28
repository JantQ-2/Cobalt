package org.cobalt.internal.ui.panel.panels

import java.awt.Color
import meteordevelopment.discordipc.DiscordIPC
import net.minecraft.client.MinecraftClient
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.util.isHoveringOver

class UISidebar : UIPanel(
  x = 0F,
  y = 0F,
  width = 70F,
  height = 600F
) {

  private val moduleButton = UIButton("/assets/cobalt/icons/box.svg") {
    println("Opening modules page")
  }

  private val hudButton = UIButton("/assets/cobalt/icons/interface.svg") {
    println("Opening HUD Editor")
  }

  private val userIcon = try {
    NVGRenderer.createImage(
      "https://mc-heads.net/avatar/${MinecraftClient.getInstance().session.uuidOrNull}/100/face.png"
    )
  } catch (e: Exception) {
    NVGRenderer.createImage("/assets/cobalt/steve.png")
  }

  init {
    components.addAll(
      listOf(moduleButton, hudButton)
    )
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(18, 18, 18).rgb, 10F)
    NVGRenderer.text("cb", x + width / 2F - 15F, y + 30F, 25F, Color(230, 230, 230).rgb)

    NVGRenderer.line(
      x + (width / 2F) - 10F,
      y + 85F,
      x + (width / 2F) + 10F,
      y + 85F,
      1F,
      Color(42, 42, 42).rgb
    )

    moduleButton
      .setSelected(true)
      .updateBounds(x + (width / 2F) - (moduleButton.width / 2F), y + 115F)
      .render()

    hudButton
      .updateBounds(x + (width / 2F) - (hudButton.width / 2F), y + 155F)
      .render()

    NVGRenderer.image(
      userIcon,
      x + (width / 2F) - 16F,
      y + height - 32F - 20F,
      32F,
      32F,
      radius = 10F
    )
  }

  private class UIButton(
    iconPath: String,
    private val onClick: () -> Unit
  ) : UIComponent(0f, 0f, 22F, 22F) {

    val image = NVGRenderer.createImage(iconPath)
    private var selected = false

    fun setSelected(selected: Boolean): UIComponent {
      this.selected = selected
      return this
    }

    override fun render() {
      val hovering = isHoveringOver(x, y, width, height)

      NVGRenderer.image(
        image,
        x, y, width, height,

        colorMask = if (hovering || selected)
          Color(79, 140, 255, 150).rgb
        else
          Color(120, 120, 120).rgb
      )
    }

    override fun mouseClicked(button: Int): Boolean {
      if (isHoveringOver(x, y, width, height) && button == 0) {
        onClick.invoke()
        return true
      }

      return false
    }

  }

}
