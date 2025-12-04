package org.cobalt.api.feat.rotation

import org.cobalt.internal.feat.rotation.DefaultRotationConfig
import org.cobalt.internal.feat.rotation.DefaultRotationParameters
import org.cobalt.internal.feat.rotation.DefaultRotations

object RotationBundle {
  var rotation: Rotation = DefaultRotations
  var config: RotationConfig = DefaultRotationConfig()
  var parameters: RotationParameters = DefaultRotationParameters()
}
