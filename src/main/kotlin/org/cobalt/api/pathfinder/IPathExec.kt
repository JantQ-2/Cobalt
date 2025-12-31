package org.cobalt.api.pathfinder

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.api.event.impl.render.WorldRenderContext

interface IPathExec {

  fun onTick(it: ClientPlayerEntity) {}
  fun onWorldRenderLast(it: WorldRenderContext, player: ClientPlayerEntity) {}
}
