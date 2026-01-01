package org.cobalt.internal.pathfinder

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.api.event.impl.render.WorldRenderContext
import org.cobalt.api.pathfinder.IPathExec

object PathExec : IPathExec {

  override fun onTick(player: ClientPlayerEntity) {}
  override fun onWorldRenderLast(ctx: WorldRenderContext, player: ClientPlayerEntity) {}

}
