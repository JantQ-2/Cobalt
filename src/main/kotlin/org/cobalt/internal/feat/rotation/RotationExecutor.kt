package org.cobalt.internal.feat.rotation

import kotlin.math.abs
import kotlin.math.max
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.MathHelper.wrapDegrees

internal object RotationExecutor {
  fun performRotation(
    rotationTargetYaw: Float,
    rotationTargetPitch: Float,
    player: ClientPlayerEntity,
    easingFactor: Float,       // base smoothing control
    endMultiplier: Float,      // modifies behavior near the end (<1 = snappy, >1 = smooth)
    tickDelayMs: Long,
    minYawOffset: Float,
    minPitchOffset: Float,
    maxYawOffset: Float,
    maxPitchOffset: Float,
  ) {
    val randomisedMaxYawOffset = RotationMath.getRandomizedOffset(minYawOffset, maxYawOffset)
    val randomisedMaxPitchOffset = RotationMath.getRandomizedOffset(minPitchOffset, maxPitchOffset)

    val initialYawDiff = wrapDegrees(rotationTargetYaw - player.yaw)
    val initialPitchDiff = rotationTargetPitch - player.pitch
    val startDistance = abs(initialYawDiff) + abs(initialPitchDiff) + 0.001f // avoid div-by-zero

    while (true) {
      if (Thread.currentThread().isInterrupted) throw InterruptedException()

      val currentYaw = player.yaw
      val currentPitch = player.pitch

      val yawDiff = wrapDegrees(rotationTargetYaw - currentYaw)
      val pitchDiff = rotationTargetPitch - currentPitch

      // Exit once inside tolerance
      if (abs(yawDiff) < randomisedMaxYawOffset && abs(pitchDiff) < randomisedMaxPitchOffset) {
        player.yaw = wrapDegrees(rotationTargetYaw)
        player.pitch = rotationTargetPitch
        break
      }

      // Adaptive easing
      val distance = abs(yawDiff) + abs(pitchDiff)
      val t = (1f - (distance / startDistance)).coerceIn(0f, 1f)

      // Blend between easingFactor (start) and easingFactor * endMultiplier (end)
      val factorScale = (1f - t) + t * endMultiplier
      val adaptiveFactor = max(0.001f, easingFactor * factorScale)

      var yawStep = yawDiff / adaptiveFactor
      var pitchStep = pitchDiff / adaptiveFactor

      // Prevent overshoot (only clamp if step would jump past target)
      if (abs(yawStep) > abs(yawDiff)) yawStep = yawDiff
      if (abs(pitchStep) > abs(pitchDiff)) pitchStep = pitchDiff

      player.yaw = wrapDegrees(currentYaw + yawStep)
      player.pitch = (currentPitch + pitchStep).coerceIn(-90.0f, 90.0f)

      Thread.sleep(tickDelayMs)
    }
  }
}
