package org.cobalt.internal.command

import org.cobalt.api.command.Command
import org.cobalt.api.command.annotation.DefaultHandler
import org.cobalt.api.command.annotation.SubCommand
import org.cobalt.internal.feature.general.NameProtect
import org.cobalt.internal.ui.screen.UIScreen

object MainCommand : Command(
  name = "cobalt",
  aliases = arrayOf("cb")
) {

  @DefaultHandler
  fun main() {
    UIScreen.openUI()
  }

  @SubCommand
  fun dev(subCmd: String) {
    when (subCmd) {
      "tnp" -> NameProtect.isEnabled = !NameProtect.isEnabled
    }
  }

}
