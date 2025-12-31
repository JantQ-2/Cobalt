package org.cobalt.internal.pathfinder

import java.awt.Color
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.cobalt.api.event.impl.render.WorldRenderContext
import org.cobalt.api.pathfinder.IPathExec
import org.cobalt.api.util.render.Render3D

object PathExec : IPathExec {

  private var currentPath: List<Vec3d>? = null
  private var currentIndex: Int = 0

  val isRunning: Boolean
    get() = currentPath != null && currentIndex < (currentPath?.size ?: 0)

  fun setPath(path: List<Vec3d>) {
    stop()
    currentPath = path
    currentIndex = 0
  }

  fun stop() {
    currentPath = null
    currentIndex = 0
  }

  override fun onTick(it: ClientPlayerEntity) {
    val path = currentPath ?: return
    if (currentIndex >= path.size) {
      stop()
      return
    }

    val target = path[currentIndex]
    val dist = Vec3d(it.x, it.y, it.z).distanceTo(target)

    if (dist < 0.5) {
      currentIndex++
    }
  }

  override fun onWorldRenderLast(it: WorldRenderContext, player: ClientPlayerEntity) {
    val path = currentPath ?: return
    if (currentIndex >= path.size) {
      stop()
      return
    }

    val target = path
    val dist = Vec3d(player.x, player.y, player.z).distanceTo(target[currentIndex])

    if (dist < 0.5) {
      currentIndex++
    }
    val ctx = it
    target.forEach {
      Render3D.drawBox(ctx, Box(it.x, it.y, it.z, it.x + 1, it.y + 1, it.z + 1), Color.BLUE)
    }
  }
}
