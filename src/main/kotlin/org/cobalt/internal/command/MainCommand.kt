package org.cobalt.internal.command

import dev.quiteboring.swift.Swift
import dev.quiteboring.swift.finder.calculate.path.AStarPathfinder
import dev.quiteboring.swift.finder.goal.Goal
import dev.quiteboring.swift.finder.movement.CalculationContext
import org.cobalt.api.command.Command
import org.cobalt.api.command.annotation.DefaultHandler
import org.cobalt.api.command.annotation.SubCommand
import org.cobalt.api.util.ChatUtils
import org.cobalt.internal.pathfinder.PathExec
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

  @SubCommand
  fun pathfind(x: Int, y: Int, z: Int) {
    Swift.executor.submit {
      val ctx = CalculationContext()
      val goal = Goal(x, y, z, ctx)
      val result =
              AStarPathfinder(
                              org.cobalt.Cobalt.mc.player!!.getX().toInt(),
                              org.cobalt.Cobalt.mc.player!!.getY().toInt(),
                              org.cobalt.Cobalt.mc.player!!.getZ().toInt(),
                              goal = goal,
                              ctx = ctx,
                              isFly = false
                      )
                      .findPath()
      if (result == null || result.points.isEmpty()) {
        ChatUtils.sendMessage("No path found!")
        return@submit
      }
      PathExec.setPath(result.points.map { it.toCenterPos() })
    }
  }
}
