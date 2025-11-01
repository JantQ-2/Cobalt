package org.cobalt.internal.module

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import org.cobalt.api.module.Module


internal object ModuleManager {

  private val modules = mutableListOf<Module>()

  fun loadModules() {
    for (entrypoint in FabricLoader.getInstance().getEntrypointContainers("cobalt", Module::class.java)) {
      val metadata: ModMetadata = entrypoint.provider.metadata
      var module: Module

      try {
        module = entrypoint.getEntrypoint()
      } catch (ex: Exception) {
        throw Exception("Could not load module: ${ex.message}")
      }

//      module.name = metadata.name
//      module.authors = metadata.authors.map {
//        it.name
//      }

      module.onInitialize()
      modules.add(module)
    }
  }

  fun getModules(): List<Module> {
    return modules
  }

}
