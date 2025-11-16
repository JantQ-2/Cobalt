package org.cobalt.internal.command

import org.cobalt.api.command.Command
import org.cobalt.api.command.annotation.DefaultHandler
import org.cobalt.api.command.annotation.SubCommand
import org.cobalt.internal.ui.screen.ConfigScreen
import org.cobalt.internal.feat.general.NameProtect

object MainCommand : Command(
  name = "cobalt",
  aliases = arrayOf("cb")
) {

  var ungrabbed = false

  @DefaultHandler
  fun main() {
    ConfigScreen.openUI()
  }

  @SubCommand
  fun grab() {
    ungrabbed = !ungrabbed
  }

  @SubCommand
  fun reload() {
    org.cobalt.internal.loader.Loader.reload()
  }

  @SubCommand
  fun unload() {
    org.cobalt.internal.loader.Loader.unload()
  }

  @SubCommand
  fun dev(subSubCommand: String) {
      if (subSubCommand == "tnp") {
        NameProtect.isEnabled = !NameProtect.isEnabled
      }
  }
}
