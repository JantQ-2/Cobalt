package org.cobalt.api.event.impl

import org.cobalt.api.event.Event
import org.cobalt.api.util.render.WorldRenderContext

abstract class WorldRenderEvent(val context: WorldRenderContext) : Event() {
  class Start(context: WorldRenderContext) : WorldRenderEvent(context)
  class Last(context: WorldRenderContext) : WorldRenderEvent(context)
}
