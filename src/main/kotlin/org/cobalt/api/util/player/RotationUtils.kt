package org.cobalt.api.util.player

import org.cobalt.api.util.EasingType
import kotlin.math.*
import org.cobalt.api.util.ChatUtils.sendDebug

/**
 * Utility object for handling player rotations, including smooth rotations, easing, and GCD fixes.
 */
object RotationUtils {
    private var rotating = false
    private var targetYaw = 0f
    private var targetPitch = 0f
    private var startYaw = 0f
    private var startPitch = 0f
    private var rotationProgress = 0f
    private var rotationSpeed = 0.1f
    private var easingType = EasingType.EASE_OUT_CUBIC
    
    /**
     * Normalizes an angle to be within the range of -180 to 180 degrees.
     *
     * @param angle The angle to normalize.
     * @return The normalized angle.
     */
    fun normalizeAngle(angle: Float): Float {
        var result = angle
        while (result > 180f) result -= 360f
        while (result < -180f) result += 360f
        return result
    }
    
    /**
     * Calculates the shortest difference between two angles.
     *
     * @param from The starting angle.
     * @param to The target angle.
     * @return The delta angle.
     */
    fun getRotationDelta(from: Float, to: Float): Float {
        var delta = normalizeAngle(to) - normalizeAngle(from)
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return delta
    }
    
    /**
     * Calculates a linear interpolation between two values.
     *
     * @param from Start value.
     * @param to End value.
     * @param progress Interpolation progress (0.0 - 1.0).
     * @return Interpolated value.
     */
    fun linearRotation(from: Float, to: Float, progress: Float): Float {
        return from + (to - from) * progress.coerceIn(0f, 1f)
    }
    
    /**
     * Calculates a quadratic Bezier interpolation.
     *
     * @param from Start value.
     * @param to End value.
     * @param progress Interpolation progress (0.0 - 1.0).
     * @param controlPoint Control point factor (default 0.5).
     * @return Interpolated value.
     */
    fun bezierRotation(from: Float, to: Float, progress: Float, controlPoint: Float = 0.5f): Float {
        val t = progress.coerceIn(0f, 1f)
        val cp = from + (to - from) * controlPoint
        return (1 - t) * ((1 - t) * from + t * cp) + t * ((1 - t) * cp + t * to)
    }
    
    /**
     * Calculates an elastic interpolation.
     *
     * @param from Start value.
     * @param to End value.
     * @param progress Interpolation progress (0.0 - 1.0).
     * @param amplitude Elastic amplitude.
     * @param period Elastic period.
     * @return Interpolated value.
     */
    fun elasticRotation(from: Float, to: Float, progress: Float, amplitude: Float = 1.0f, period: Float = 0.3f): Float {
        val t = progress.coerceIn(0f, 1f)
        if (t == 0f || t == 1f) return if (t == 0f) from else to
        val delta = to - from
        val s = period / (2 * PI)
        return from + delta * (amplitude * (2.0).pow(-10.0 * t) * sin((t - s) * (2 * PI) / period) + 1).toFloat()
    }
    
    /**
     * Calculates a sinusoidal interpolation.
     *
     * @param from Start value.
     * @param to End value.
     * @param progress Interpolation progress (0.0 - 1.0).
     * @return Interpolated value.
     */
    fun sinusoidalRotation(from: Float, to: Float, progress: Float): Float {
        val t = progress.coerceIn(0f, 1f)
        return from + (to - from) * (1 - cos(t * PI.toFloat())) / 2
    }
    
    /**
     * Calculates an exponential interpolation.
     *
     * @param from Start value.
     * @param to End value.
     * @param progress Interpolation progress (0.0 - 1.0).
     * @param exponent Exponent value (default 2.0).
     * @param easeIn True for ease-in, false for ease-out.
     * @return Interpolated value.
     */
    fun exponentialRotation(from: Float, to: Float, progress: Float, exponent: Float = 2.0f, easeIn: Boolean = true): Float {
        var t = progress.coerceIn(0f, 1f)
        t = if (easeIn) t.pow(exponent) else 1 - (1 - t).pow(exponent)
        return from + (to - from) * t
    }
    
    /**
     * Calculates rotation using a specific [EasingType].
     *
     * @param from Start value.
     * @param to End value.
     * @param progress Interpolation progress (0.0 - 1.0).
     * @param easing The easing type to use.
     * @return Interpolated value.
     */
    fun easedRotation(from: Float, to: Float, progress: Float, easing: EasingType): Float {
        return easing.apply(from, to, progress)
    }
    
    /**
     * Start a smooth rotation to the target angles.
     *
     * @param yaw Target yaw angle.
     * @param pitch Target pitch angle.
     * @param speed Rotation speed (0.0-1.0, default 0.1).
     * @param easing Easing function to use (default LINEAR).
     */
    fun rotateTo(
        yaw: Float,
        pitch: Float,
        speed: Float = 0.1f,
        easing: EasingType = EasingType.LINEAR
    ) {
        val mc = org.cobalt.Cobalt.mc
        val player = mc.player ?: return

        fun Float.bad(): Boolean = isNaN() || isInfinite() || (this != 0f && kotlin.math.abs(this) < 1e-30f)

        if (yaw.bad() || pitch.bad()) {
            sendDebug("Invalid rotation target: yaw=$yaw, pitch=$pitch")
            return
        }

        fun norm(a: Float): Float {
            var x = a % 360f
            if (x < -180f) x += 360f
            if (x > 180f) x -= 360f
            return x
        }

        val safeYaw = norm(yaw)
        val safePitch = pitch.coerceIn(-90f, 90f)

        if (safeYaw.isNaN() || safePitch.isNaN()) {
            sendDebug("Rotation failed sanity normalization.")
            return
        }

        sendDebug("Rotating... (yaw: $safeYaw, pitch: $safePitch)")

        startYaw = norm(player.yaw)
        startPitch = player.pitch

        targetYaw = safeYaw
        targetPitch = safePitch

        rotationProgress = 0f
        rotationSpeed = speed.coerceIn(0.01f, 1f)
        easingType = easing
        rotating = true

        sendDebug("set rotating to true")
    }
    
    /**
     * Snap to the specified yaw and pitch instantly.
     *
     * @param yaw Target yaw.
     * @param pitch Target pitch.
     */
    fun setRotations(yaw: Float, pitch: Float) {
        if (yaw.isNaN() || yaw.isInfinite() || pitch.isNaN() || pitch.isInfinite()) return
        
        val mc = org.cobalt.Cobalt.mc
        mc.player?.yaw = normalizeAngle(yaw)
        mc.player?.pitch = pitch.coerceIn(-90f, 90f)
        rotating = false
    }
    
    /**
     * Stops the current rotation.
     */
    fun stopRotation() {
        rotating = false
    }
    
    /**
     * Checks if a rotation is currently in progress.
     *
     * @return True if rotating, false otherwise.
     */
    fun isRotating(): Boolean = rotating
    
    /**
     * Gets the current progress of the rotation.
     *
     * @return Progress value between 0.0 and 1.0.
     */
    fun getProgress(): Float = rotationProgress

    /**
     * Applies the mouse sensitivity GCD fix to rotations to prevent anti-cheat flags.
     *
     * @param rotation The target rotation.
     * @param prevRotation The previous rotation.
     * @param min Optional minimum bound.
     * @param max Optional maximum bound.
     * @return The adjusted rotation value.
     */
    fun applyGCD(rotation: Float, prevRotation: Float, min: Float? = null, max: Float? = null): Float {
        val mc = org.cobalt.Cobalt.mc
        val sensitivity = mc.options.mouseSensitivity.value
        val f = sensitivity * 0.6 + 0.2
        val gcd = f * f * f * 1.2
        
        val delta = getRotationDelta(prevRotation, rotation)
        val roundedDelta = (delta / gcd).roundToInt() * gcd
        var result = prevRotation + roundedDelta
        
        if (max != null && result > max) {
            result -= gcd
        }
        if (min != null && result < min) {
            result += gcd
        }
        
        return result.toFloat()
    }

    @org.cobalt.api.event.annotation.SubscribeEvent
    fun onWorldRender(event: org.cobalt.api.event.impl.render.WorldRenderEvent.Start) {
        if (!rotating) return
        
        val mc = org.cobalt.Cobalt.mc
        val player = mc.player ?: return
        rotationProgress += rotationSpeed
        
        if (rotationProgress >= 1.0f) {
            player.yaw = normalizeAngle(applyGCD(targetYaw, player.yaw))
            player.pitch = applyGCD(targetPitch, player.pitch, -90f, 90f)
            rotating = false
            rotationProgress = 1.0f
            return
        }
        
        val yawDelta = getRotationDelta(startYaw, targetYaw)
        val easedYawDelta = easedRotation(0f, yawDelta, rotationProgress, easingType)
        val currentYaw = normalizeAngle(startYaw + easedYawDelta)

        val pitchDelta = targetPitch - startPitch
        val easedPitchDelta = easedRotation(0f, pitchDelta, rotationProgress, easingType)
        val currentPitch = (startPitch + easedPitchDelta).coerceIn(-90f, 90f)
        
        player.yaw = normalizeAngle(applyGCD(currentYaw, player.yaw))
        player.pitch = applyGCD(currentPitch, player.pitch, -90f, 90f).coerceIn(-90f, 90f)
    }
}