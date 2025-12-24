package org.cobalt.internal.feature.rotation.strategy

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.internal.feature.rotation.DefaultRotationParameters

internal interface RotationStrategy {
  fun perform(
      yaw: Float,
      pitch: Float,
      player: ClientPlayerEntity,
      parameters: DefaultRotationParameters,
  )
}
