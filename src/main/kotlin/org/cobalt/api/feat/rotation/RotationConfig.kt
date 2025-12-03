package org.cobalt.api.feat.rotation

abstract class RotationConfig(var implementation: RotationConfig? = DefaultRotationConfig()) {
  open fun validate(): Boolean {
    return false
  }

}
