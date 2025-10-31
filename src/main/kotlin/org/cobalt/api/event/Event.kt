package org.cobalt.api.event

class Event(private val cancellable: Boolean = true) {

  var isCancelled: Boolean = false

  fun isCancelled(): Boolean {
    if (!cancellable) {
      return false
    }

    return isCancelled
  }

  fun post(): Boolean {
    EventBus.post(this)
    return isCancelled
  }

}

