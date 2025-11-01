package org.cobalt.api.feat.rotation

import org.cobalt.internal.feat.rotation.DefaultRotationConfig

abstract class RotationConfig(var implementation: RotationConfig? = DefaultRotationConfig()) {
  open fun validate(): Boolean {
    return false
  }

}
