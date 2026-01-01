package org.cobalt.internal.command

import org.cobalt.api.command.Command
import org.cobalt.api.command.annotation.DefaultHandler
import org.cobalt.api.command.annotation.SubCommand
import org.cobalt.internal.rotation.EasingType
import org.cobalt.internal.rotation.RotationExec
import org.cobalt.internal.rotation.strategy.TimedEaseStrategy
import org.cobalt.internal.ui.screen.UIConfig

internal object MainCommand : Command(
  name = "cobalt",
  aliases = arrayOf("cb")
) {

  @DefaultHandler
  fun main() {
    UIConfig.openUI()
  }

  @SubCommand
  fun rotate(yaw: Double, pitch: Double, duration: Int) {
    RotationExec.rotateTo(
      yaw.toFloat(),
      pitch.toFloat(),
      TimedEaseStrategy(
        yawEaseType = EasingType.EASE_OUT_EXPO,
        pitchEaseType = EasingType.EASE_OUT_EXPO,
        duration = duration.toLong()
      )
    )
  }

}
