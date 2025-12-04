package org.cobalt.api.feat.pathfinder

import org.cobalt.internal.feat.pathfinding.DefaultPathfinder
import org.cobalt.internal.feat.pathfinding.DefaultPathfindingConfig

object PathfindingBundle {
  var pathFinding: Pathfinding = DefaultPathfinder
  var config: PathfindingConfig = DefaultPathfindingConfig()
}
