package org.cobalt.internal.loader

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.spongepowered.asm.mixin.Mixins

class Core : PreLaunchEntrypoint {

  override fun onPreLaunch() {
    loadAddons()
  }

  fun loadAddons() {
    val addonsDir = Paths.get("config/cobalt/addons/")
    if (!Files.isDirectory(addonsDir)) {
      Files.createDirectories(addonsDir)
      return
    }

    Files.newDirectoryStream(addonsDir, "*.jar").use { stream ->
      for (jarPath in stream) {
        FabricLauncherBase.getLauncher().addToClassPath(jarPath)
      }
    }

    try {
      Files.newDirectoryStream(addonsDir, "*.jar").use { stream ->
        for (jarPath in stream) {
          registerAddonMixins(jarPath)
        }
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun registerAddonMixins(jarPath: Path) {
    ZipFile(jarPath.toFile()).use { zipFile ->
      val entries = zipFile.entries()

      while (entries.hasMoreElements()) {
        val entry: ZipEntry = entries.nextElement()
        val name = entry.getName()

        if (name.endsWith(".mixins.json") && name != "cobalt.mixins.json") {
          if (name.contains("client") && FabricLoader.getInstance().environmentType != EnvType.CLIENT) {
            continue
          }

          synchronized(Mixins::class.java) {
            Mixins.addConfiguration(name)
          }
        }
      }
    }
  }
}
