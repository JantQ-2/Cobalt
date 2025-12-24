package org.cobalt.internal.feature.rotation

import org.cobalt.api.util.rotation.RotationParameters

internal class DefaultRotationParameters : RotationParameters() {
  var yawMaxOffset: Float = 0f
  var pitchMaxOffset: Float = 0f
  var canOvershoot: Boolean = true
}
