package org.cobalt.api.notification

interface NotificationAPI {
  fun sendNotification(title: String, description: String, duration: Long = 5000L)
  fun clear()
}
