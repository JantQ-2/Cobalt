package org.cobalt.internal.feat.rotation

import org.cobalt.api.feat.rotation.RotationConfig

internal class DefaultRotationConfig : RotationConfig() {
  val easeFactor: Float = 8f
  val tickDelay: Long = 10
  val defaultEndMultiplier: Float = .5f
  val accurateEndMultiplier: Float = 1.2f
  val overshootDegrees = Pair(5f, 15f)

  /* TODO: Please make it work like this:
  easeFactor by SliderSetting("Easing factor", 8.0, 2.0, 20.0, true),
  rotationDelay by SliderSetting("Rotation update delay", 10.0, 2.0, 20.0, true),
  rotationThreshold by SliderSetting("Rotation Complete Threshold", 0.5, 0.1, 0.9, true),

  overshootDegrees by RangeSetting("Overshoot Degrees", Pair(5.0, 15.0), 1.0, 80.0, true),
  overshootPause by RangeSetting("Overshoot Pause Time (ms)", Pair(50.0, 100.0), 0.0, 250.0, false),
  */

  override fun validate(): Boolean {
    // Validate slider settings
    if (easeFactor !in 2.0..20.0) return false
    if (tickDelay !in 2..20) return false

    // Validate range settings
    val (overshootLow, overshootHigh) = overshootDegrees
    if (overshootLow !in 1.0..80.0 || overshootHigh !in 1.0 .. 80.0 || overshootLow >= overshootHigh) return false
    return accurateEndMultiplier > defaultEndMultiplier && defaultEndMultiplier > 0
  }
}
