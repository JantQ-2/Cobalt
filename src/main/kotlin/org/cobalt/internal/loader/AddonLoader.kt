package org.cobalt.internal.loader

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.util.zip.ZipFile
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.cobalt.api.addon.Addon
import org.cobalt.api.addon.AddonMetadata
import org.cobalt.api.command.CommandManager
import org.cobalt.api.module.ModuleManager
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.api.util.ui.helper.Image
import org.spongepowered.asm.mixin.Mixins

object AddonLoader {

  private val addonsDir: Path = Paths.get("config/cobalt/addons/")
  private val addons = mutableListOf<Pair<AddonMetadata, Addon>>()
  private val loadedJars = mutableMapOf<Path, FileTime>()
  private val gson = Gson()

  fun findAddons() {
    if (FabricLauncherBase.getLauncher().isDevelopment) {
      for (entry in FabricLoader.getInstance().getEntrypointContainers("cobalt", Addon::class.java)) {
        val modMeta = entry.provider.metadata
        val metadata = AddonMetadata(
          id = modMeta.id,
          name   = modMeta.name,
          version = modMeta.version?.toString() ?: "unknown",
          entrypoints = listOf(entry.entrypoint.javaClass.name),
          mixins = listOf()
        )

        val addonInstance: Addon = try {
          entry.entrypoint
        } catch (e: Throwable) {
          throw RuntimeException("Failed to initialize addon \"${modMeta.name}\"", e)
        }

        addons += metadata to addonInstance
      }
    }

    if (!Files.isDirectory(addonsDir)) {
      Files.createDirectories(addonsDir)
      return
    }

    try {
      Files.newDirectoryStream(addonsDir, "*.jar").use { stream ->
        for (jarPath in stream) {
          FabricLauncherBase.getLauncher().addToClassPath(jarPath)
          loadAddon(jarPath)
          loadedJars[jarPath] = Files.getLastModifiedTime(jarPath)
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun loadAddon(jarPath: Path): AddonMetadata {
    ZipFile(jarPath.toFile()).use { zip ->
      val jsonEntry = zip.getEntry("cobalt.addon.json")
        ?: throw IllegalStateException("Missing cobalt.addon.json in $jarPath")

      val metadata = zip.getInputStream(jsonEntry).use { input ->
        gson.fromJson(input.reader(), AddonMetadata::class.java)
      }

      synchronized(Mixins::class.java) {
        for (mixin in metadata.mixins) {
          Mixins.addConfiguration(mixin)
        }
      }

      for (entrypoint in metadata.entrypoints) {
        val classPath = entrypoint.replace('.', '/') + ".class"

        if (zip.getEntry(classPath) == null) {
          throw IllegalStateException(
            "Entrypoint class '$entrypoint' does not exist inside ${jarPath.fileName}"
          )
        }

        val instance = Class.forName(entrypoint).let {
          try {
            it.getField("INSTANCE").get(null)
          } catch (_: NoSuchFieldException) {
            val constructor = it.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
          }
        }

        if (instance !is Addon) {
          throw IllegalStateException(
            "Entrypoint '$entrypoint' must implement Addon"
          )
        }

        addons += metadata to instance
      }

      return metadata
    }
  }

  fun getAddons(): List<Pair<AddonMetadata, Addon>> {
    return addons.toList()
  }

  fun getAddonIcon(addonId: String): Image? {
    return NVGRenderer.createImage(
      addons.find { it.first.id == addonId }?.first?.icon ?: return null
    )
  }

  /**
   * Scans the addons directory for new or updated addon JARs and loads them.
   * Returns a list of newly loaded addons.
   */
  fun scanForNewAddons(): List<Pair<AddonMetadata, Addon>> {
    if (!Files.isDirectory(addonsDir)) {
      Files.createDirectories(addonsDir)
      return emptyList()
    }

    val newAddons = mutableListOf<Pair<AddonMetadata, Addon>>()

    try {
      Files.newDirectoryStream(addonsDir, "*.jar").use { stream ->
        for (jarPath in stream) {
          val lastModified = Files.getLastModifiedTime(jarPath)
          val previousModified = loadedJars[jarPath]

          // Check if this is a new file or if it has been modified
          if (previousModified == null || lastModified > previousModified) {
            try {
              // Add to classpath if not already added
              if (previousModified == null) {
                FabricLauncherBase.getLauncher().addToClassPath(jarPath)
              }

              // If modified, we can't truly reload due to classloader limitations
              // but we can at least load new addons
              if (previousModified == null) {
                loadAddon(jarPath)
                loadedJars[jarPath] = lastModified
                
                // Get the most recently added addon (the one we just loaded)
                val newAddon = addons.last()
                newAddons.add(newAddon)
                
                println("Hot-loaded new addon: ${newAddon.first.name}")
              } else {
                println("Addon ${jarPath.fileName} was modified, but hot-reload of modified addons requires a restart")
              }
            } catch (e: Exception) {
              println("Failed to load addon ${jarPath.fileName}: ${e.message}")
              e.printStackTrace()
            }
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    return newAddons
  }

  /**
   * Initializes newly loaded addons by calling their onLoad() and registering modules
   */
  fun initializeAddons(newAddons: List<Pair<AddonMetadata, Addon>>) {
    newAddons.forEach { (metadata, addon) ->
      try {
        addon.onLoad()
        ModuleManager.addModules(addon.getModules())
        println("Initialized addon: ${metadata.name}")
      } catch (e: Exception) {
        println("Failed to initialize addon ${metadata.name}: ${e.message}")
        e.printStackTrace()
      }
    }
    
    // Re-register all commands to include new addon commands
    if (newAddons.isNotEmpty()) {
      CommandManager.reregisterCommands()
    }
  }

}
