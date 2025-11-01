package org.cobalt.api.feat.rotation

import net.minecraft.client.network.ClientPlayerEntity

interface Rotation {
  val name: String

  fun rotateTo(
    yaw: Float,
    pitch: Float,
    player: ClientPlayerEntity,
    parameters: RotationParameters,
  )
}
