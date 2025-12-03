package org.cobalt.internal.feat.rotation

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.api.feat.rotation.DefaultRotationParameters

interface RotationStrategy {
  fun perform(
      yaw: Float,
      pitch: Float,
      player: ClientPlayerEntity,
      parameters: DefaultRotationParameters,
  )
}
