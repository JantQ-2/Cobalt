package org.cobalt.api.event

import java.util.concurrent.ConcurrentHashMap
import java.lang.reflect.Method
import org.cobalt.api.event.annotation.SubscribeEvent

object EventBus {

  private val listeners = ConcurrentHashMap<Class<*>, MutableList<ListenerData>>()
  private val registered = mutableSetOf<Any>()

  fun register(obj: Any) {
    if (obj in registered) return

    obj::class.java.declaredMethods.forEach { method ->
      if (method.isAnnotationPresent(SubscribeEvent::class.java)) {
        val params = method.parameterTypes
        require(params.size == 1 && Event::class.java.isAssignableFrom(params[0])) {
          "Invalid Method"
        }

        method.isAccessible = true
        val priority = method.getAnnotation(SubscribeEvent::class.java).priority

        listeners.computeIfAbsent(params[0]) { mutableListOf() }
          .add(ListenerData(obj, method, priority))

        listeners[params[0]]?.sortByDescending { it.priority }
      }
    }

    registered.add(obj)
  }

  fun unregister(obj: Any) {
    if (obj !in registered) return
    listeners.values.forEach { it.removeIf { data -> data.instance === obj } }
    registered.remove(obj)
  }

  fun post(event: Event): Event {
    val eventClass = event::class.java
    val applicable = listeners.flatMap { (type, methods) ->
      if (type.isAssignableFrom(eventClass)) methods else emptyList()
    }.sortedByDescending { it.priority }

    applicable.forEach { data ->
      try {
        data.method.invoke(data.instance, event)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    return event
  }

  private data class ListenerData(val instance: Any, val method: Method, val priority: Int)

}
