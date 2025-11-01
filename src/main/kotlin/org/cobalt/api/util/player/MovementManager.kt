package org.cobalt.api.util.player

object MovementManager {
  @JvmField
  @Volatile
  var isLookLocked = false // False: Player can control camera. True: Player can't control the camera
}
