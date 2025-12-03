package org.cobalt.api.feat.rotation

class DefaultRotationParameters : RotationParameters() {
  var yawMaxOffset: Float = 0f
  var pitchMaxOffset: Float = 0f
  var canOvershoot: Boolean = true
}
