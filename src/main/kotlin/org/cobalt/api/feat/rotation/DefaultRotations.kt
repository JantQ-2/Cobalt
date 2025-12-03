package org.cobalt.api.feat.rotation

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.api.util.player.MovementManager
import org.cobalt.internal.feat.rotation.strategy.OvershootRotationStrategy
import org.cobalt.internal.feat.rotation.strategy.SimpleRotationStrategy
import kotlin.concurrent.thread
import kotlin.random.Random
import org.cobalt.api.util.ChatUtils
import org.cobalt.internal.feat.rotation.RotationMath
import org.cobalt.internal.feat.rotation.RotationStrategy

object DefaultRotations : Rotation {
  override val name: String
    get() = "Default"

  @Volatile
  private var rotationThread: Thread? = null
  private val lock = Any()

  fun stop() {
    synchronized(lock) {
      rotationThread?.interrupt()
      MovementManager.isLookLocked = false
      rotationThread = null
    }
  }

  fun shouldOvershoot(targetYaw: Float, targetPitch: Float, player: ClientPlayerEntity): Boolean {
    val probability = if (RotationMath.isWithinFov(targetYaw, targetPitch, player)) 0.03 else 0.98
    return Random.nextDouble() < probability
  }

  override fun rotateTo(
    yaw: Float,
    pitch: Float,
    player: ClientPlayerEntity,
    parameters: RotationParameters,
  ) {
    stop()

    if (!DefaultRotationConfig().validate()) {
      ChatUtils.sendMessage("Invalid rotation config")
      return
    }

    if (parameters !is DefaultRotationParameters) {
      ChatUtils.sendMessage("Invalid rotation parameters. Please contact the cobalt developers about this!")
      return
    }

    val strategy: RotationStrategy =
      if (parameters.canOvershoot && shouldOvershoot(yaw, pitch, player)) OvershootRotationStrategy()
      else SimpleRotationStrategy()

    synchronized(lock) {
      if (rotationThread == null || !rotationThread!!.isAlive) {
        MovementManager.isLookLocked = true
        rotationThread = thread(start = true, name = "PlayerRotationThread") {
          try {
            strategy.perform(
              yaw,
              pitch,
              player,
              parameters
            )
          } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
          } finally {
            MovementManager.isLookLocked = false
            synchronized(lock) { rotationThread = null }
          }
        }
      }
    }
  }

}
