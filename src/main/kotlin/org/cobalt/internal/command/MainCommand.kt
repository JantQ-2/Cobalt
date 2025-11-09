package org.cobalt.internal.command

import org.cobalt.api.command.Command
import org.cobalt.api.command.annotation.DefaultHandler
import org.cobalt.api.command.annotation.SubCommand
import org.cobalt.internal.ui.screen.ConfigScreen

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

}
