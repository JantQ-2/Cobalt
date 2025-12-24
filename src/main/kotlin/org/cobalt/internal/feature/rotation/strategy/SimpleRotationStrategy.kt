package org.cobalt.internal.feature.rotation.strategy

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.internal.feature.rotation.DefaultRotationConfig
import org.cobalt.internal.feature.rotation.DefaultRotationParameters
import org.cobalt.internal.feature.rotation.RotationExecutor

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
