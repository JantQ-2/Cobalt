package org.cobalt.api.feat.pathfinder

import org.cobalt.internal.feat.pathfinding.DefaultPathfindingConfig

abstract class PathfindingConfig(var implementation: PathfindingConfig? = DefaultPathfindingConfig()) {
  open fun validate(): Boolean {
    return false
  }

}
