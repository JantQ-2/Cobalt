package org.cobalt.internal.rpc

import dev.cbyrne.kdiscordipc.KDiscordIPC
import dev.cbyrne.kdiscordipc.core.event.impl.ReadyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal object RichPresenceManager {
  private val ipc = KDiscordIPC("1406359679772266608")

  fun startRpc() {
    CoroutineScope(Dispatchers.IO).launch {
      println("Starting RPC")
      ipc.on<ReadyEvent> {
        ipc.activityManager.setActivity("A person with great taste.") {}
      }

      println("Started RPC")
      ipc.connect()
    }
  }

}
