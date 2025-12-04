package org.cobalt.internal.feat.rotation

import org.cobalt.api.feat.rotation.RotationParameters

internal class DefaultRotationParameters : RotationParameters() {
  var yawMaxOffset: Float = 0f
  var pitchMaxOffset: Float = 0f
  var canOvershoot: Boolean = true
}
