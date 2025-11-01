package org.cobalt.internal.feat.rotation.strategy

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.internal.feat.rotation.DefaultRotationConfig
import org.cobalt.internal.feat.rotation.DefaultRotationParameters
import org.cobalt.internal.feat.rotation.RotationExecutor
import org.cobalt.internal.feat.rotation.RotationStrategy

internal class SimpleRotationStrategy : RotationStrategy {
  override fun perform(
    yaw: Float,
    pitch: Float,
    player: ClientPlayerEntity,
    parameters: DefaultRotationParameters,
  ) {
    val config = DefaultRotationConfig()

    RotationExecutor.performRotation(
      yaw,
      pitch,
      player,
      config.easeFactor,
      config.defaultEndMultiplier,
      config.tickDelay,
      .1f,
      .1f,
      parameters.yawMaxOffset,
      parameters.pitchMaxOffset
    )
  }
}
