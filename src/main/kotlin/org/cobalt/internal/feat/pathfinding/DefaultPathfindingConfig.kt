package org.cobalt.internal.feat.pathfinding

import org.cobalt.api.feat.pathfinder.PathfindingConfig

internal class DefaultPathfindingConfig : PathfindingConfig() {
  override fun validate(): Boolean {
    return true
  }
}
