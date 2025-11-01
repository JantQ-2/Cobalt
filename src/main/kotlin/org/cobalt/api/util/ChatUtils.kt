package org.cobalt.api.util

import kotlin.math.roundToInt
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting

object ChatUtils {
  fun sendDebug(message: String) {
    mc.inGameHud.chatHud.addMessage(
      Text.empty().append(debugPrefix)
        .append(Text.literal("${Formatting.RESET}$message"))
    )
  }

  fun sendMessage(message: String) {
    mc.inGameHud.chatHud.addMessage(
      Text.empty().append(prefix)
        .append(Text.literal("${Formatting.RESET}$message"))
    )
  }

  fun buildGradient(text: String, startRgb: Int, endRgb: Int): MutableText {
    val result = Text.empty()
    val length = text.length

    if (length <= 1) {
      return Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(startRgb)))
    }

    val sr = (startRgb shr 16) and 0xFF
    val sg = (startRgb shr 8) and 0xFF
    val sb = startRgb and 0xFF

    val er = (endRgb shr 16) and 0xFF
    val eg = (endRgb shr 8) and 0xFF
    val eb = endRgb and 0xFF

    for (i in text.indices) {
      val ratio = i.toDouble() / (length - 1)

      val r = (sr + ratio * (er - sr)).roundToInt()
      val g = (sg + ratio * (eg - sg)).roundToInt()
      val b = (sb + ratio * (eb - sb)).roundToInt()

      val rgb = (r shl 16) or (g shl 8) or b

      val charText = Text.literal(text[i].toString())
        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)))

      result.append(charText)
    }

    return result
  }

  private val mc = MinecraftClient.getInstance()
  private val prefix = Text.literal("${Formatting.DARK_GRAY}[")
    .append(buildGradient("Cobalt", 0x4A90E2, 0x2C5DA3))
    .append(Text.literal("${Formatting.DARK_GRAY}] "))

  private val debugPrefix = Text.literal("${Formatting.DARK_GRAY}[")
    .append(buildGradient("Cobalt Debug", 0x4A90E2, 0x2C5DA3))
    .append(Text.literal("${Formatting.DARK_GRAY}] "))

}
