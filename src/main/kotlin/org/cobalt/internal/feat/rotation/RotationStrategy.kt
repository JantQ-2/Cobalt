package org.cobalt.internal.feat.rotation

import net.minecraft.client.network.ClientPlayerEntity

interface RotationStrategy {
  fun perform(
    yaw: Float,
    pitch: Float,
    player: ClientPlayerEntity,
    parameters: DefaultRotationParameters,
  )
}
