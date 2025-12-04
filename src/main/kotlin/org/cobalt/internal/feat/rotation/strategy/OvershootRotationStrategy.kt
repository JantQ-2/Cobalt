package org.cobalt.internal.feat.rotation.strategy

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.internal.feat.rotation.DefaultRotationConfig
import org.cobalt.api.util.ChatUtils
import org.cobalt.internal.feat.rotation.DefaultRotationParameters
import org.cobalt.internal.feat.rotation.RotationExecutor
import org.cobalt.internal.feat.rotation.RotationMath
import org.cobalt.internal.feat.rotation.RotationStrategy

internal class OvershootRotationStrategy : RotationStrategy {
  override fun perform(
    yaw: Float,
    pitch: Float,
    player: ClientPlayerEntity,
    parameters: DefaultRotationParameters,
  ) {
    val config = DefaultRotationConfig()
    val calculatedParameters = RotationMath.calculateOvershootTargets(
      yaw,
      pitch,
      player.yaw,
      player.pitch,
      config.overshootDegrees.first,
      config.overshootDegrees.second,
    )

    ChatUtils.sendDebug("Overshooting/undershooting rotation.")
    RotationExecutor.performRotation(
      calculatedParameters.overshootTargetYaw,
      calculatedParameters.overshootTargetPitch,
      player,
      config.easeFactor / 1f,
      config.defaultEndMultiplier,
      config.tickDelay,
      .5f,
      .3f,
      parameters.yawMaxOffset,
      parameters.pitchMaxOffset,
    )

    ChatUtils.sendDebug("Correcting rotation.")
    RotationExecutor.performRotation(
      yaw,
      pitch,
      player,
      config.easeFactor * 2,
      config.accurateEndMultiplier,
      config.tickDelay / 2,
      .1f,
      .1f,
      parameters.yawMaxOffset,
      parameters.pitchMaxOffset
    )
  }

}
