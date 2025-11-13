package org.cobalt.api.module

object ModuleManager {

  private val moduleList = mutableListOf<Module>()

  fun addModules(vararg modules: Module) {
    moduleList.addAll(listOf(*modules))
  }

  fun getModules(): List<Module> {
    return moduleList
  }

  fun getCategories(): List<Category> {
    return moduleList
      .map { it.category }
      .distinct()
      .sortedBy { it.name }
  }
  
  fun clearModules() {
    moduleList.clear()
  }

}
