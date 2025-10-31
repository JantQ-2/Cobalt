package org.cobalt.api.event.impl

abstract class TickEvent {
  class Start(): TickEvent()
  class End(): TickEvent()
}
