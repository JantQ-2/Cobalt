package org.cobalt.api.feat.pathfinder

abstract class PathfindingConfig(var implementation: PathfindingConfig? = DefaultPathfindingConfig()) {
  open fun validate(): Boolean {
    return false
  }

}
