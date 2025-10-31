package org.cobalt

import net.fabricmc.api.ClientModInitializer
import org.cobalt.loader.Loader

class CoreMod : ClientModInitializer{

  override fun onInitializeClient() {
    /** Call constructor of each class which initializes the class */
    Loader
  }

}
