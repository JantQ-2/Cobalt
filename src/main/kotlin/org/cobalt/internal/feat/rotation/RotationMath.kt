package org.cobalt.internal.feat.rotation

import kotlin.math.*
import kotlin.random.Random
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.MathHelper.wrapDegrees
import org.cobalt.CoreMod.mc

internal object RotationMath {
  data class OvershootParams(val overshootTargetYaw: Float, val overshootTargetPitch: Float)

  fun calculateOvershootTargets(
    targetYaw: Float,
    targetPitch: Float,
    initialYaw: Float,
    initialPitch: Float,
    minOverShoot: Float,
    maxOverShoot: Float,
  ): OvershootParams {
    val initialYawDiff = wrapDegrees(targetYaw - initialYaw)
    val initialPitchDiff = targetPitch - initialPitch

    val yawOvershoot = getRandomizedOffset(minOverShoot, maxOverShoot)
    val pitchOvershoot = getRandomizedOffset(minOverShoot, maxOverShoot)

    val yawAmountScale = abs(initialYawDiff) / 180f
    val pitchAmountScale = abs(initialPitchDiff) / 90f

    val overshootYaw = targetYaw + yawOvershoot * yawAmountScale * sign(initialYawDiff)
    val overshootPitch = (targetPitch + pitchOvershoot * pitchAmountScale * sign(initialPitchDiff))
      .coerceIn(-90f, 90f)

    return OvershootParams(overshootYaw, overshootPitch)
  }

  fun getRandomizedOffset(min: Float, max: Float): Float {
    val effectiveMin = maxOf(min, 0.01f)
    val effectiveMax = maxOf(max, effectiveMin)

    if (effectiveMin == effectiveMax) {
      return effectiveMin
    }

    return Random.nextFloat() * (effectiveMax - effectiveMin) + effectiveMin
  }

  fun isWithinFov(targetYaw: Float, targetPitch: Float, player: ClientPlayerEntity): Boolean {
    val yawDiff = wrapDegrees(targetYaw - player.yaw)
    val pitchDiff = targetPitch - player.pitch

    val verticalFov = mc.options.fov.value.toFloat()
    val horizontalFov = verticalToHorizontalFov(verticalFov, getAspectRatio())

    return abs(yawDiff) <= horizontalFov / 2f && abs(pitchDiff) <= verticalFov / 2f
  }

  fun verticalToHorizontalFov(verticalFov: Float, aspectRatio: Float): Float {
    val verticalFovRad = verticalFov * (PI / 180.0)
    val horizontalFovRad = 2.0 * atan(tan(verticalFovRad / 2.0) * aspectRatio)
    return (horizontalFovRad * (180.0 / PI)).toFloat()
  }

  fun getAspectRatio(): Float {
    val window = mc.window
    if (window.height <= 0) {
      return 16f / 9
    }

    return window.width.toFloat() / window.height.toFloat()
  }
}
