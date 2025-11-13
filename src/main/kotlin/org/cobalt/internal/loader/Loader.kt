package org.cobalt.internal.loader

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import org.cobalt.api.addon.Addon
import org.cobalt.api.util.ChatUtils
import java.nio.file.*
import org.cobalt.api.module.ModuleManager
import java.util.concurrent.ConcurrentHashMap
import org.cobalt.internal.ui.screen.ConfigScreen
import java.util.jar.JarFile
import kotlin.io.path.*

object Loader {
    private val addonsDir = Paths.get("config/cobalt/addons")

    private val loadedAddons = ConcurrentHashMap<Path, AddonInfo>()
    private val activeAddons = mutableListOf<Addon>()

    data class AddonInfo(
        val path: Path,
        val lastModified: Long,
        val modId: String?,
        val entrypoints: List<Any>,
        val loaded: Boolean
    )

    data class AddonMetadata(
        val id: String,
        val name: String,
        val version: String,
        val author: String = "Unknown",
        val description: String = "",
        val entrypoints: List<String> = emptyList(),
        val classes: List<String> = emptyList(),
        val dependencies: List<String> = emptyList()
    )

    fun initialize() {
        if (!addonsDir.exists()) {
            addonsDir.createDirectories()
        }

        loadExternalAddons()

        println("initialized with ${getActiveAddonCount()} addon(s)")
    }

    private fun loadExternalAddons() {
        println("Scanning for external addons in: ${addonsDir.toAbsolutePath()}")
        addonsDir.listDirectoryEntries("*.jar").forEach { jarPath ->
            loadExternalAddon(jarPath)
        }
    }

    private fun loadExternalAddon(jarPath: Path) {
        try {
            if (!jarPath.exists() || !jarPath.isRegularFile()) {
                println("Invalid addon path: $jarPath")
                return
            }

            val lastModified = jarPath.getLastModifiedTime().toMillis()
            val existing = loadedAddons[jarPath]

            // if (existing != null && existing.lastModified == lastModified && existing.loaded) {
            //     ChatUtils.sendDebug("addon ${jarPath.fileName} is already loaded, skipping")
            //     return
            // }

            FabricLauncherBase.getLauncher().addToClassPath(jarPath)

            val (modId, entrypoints) = parseAndLoadEntrypoints(jarPath)

            loadedAddons[jarPath] = AddonInfo(
                path = jarPath,
                lastModified = lastModified,
                modId = modId,
                entrypoints = entrypoints,
                loaded = true
            )

        } catch (e: Exception) {
            println("Failed to load addon: ${jarPath.fileName} - ${e.message}")
            e.printStackTrace()
        }
    }

    private fun parseAndLoadEntrypoints(jarPath: Path): Pair<String?, List<Any>> {
        val loadedEntrypoints = mutableListOf<Any>()
        var addonId: String? = null

        try {
            JarFile(jarPath.toFile()).use { jar ->
                val entry = jar.getEntry("cobalt.addon.json") ?: run {
                    println("No cobalt.addon.json found in ${jarPath.fileName}, skipping!")
                    return Pair(null, emptyList())
                }

                val jsonText = jar.getInputStream(entry).bufferedReader().readText()
                val metadata = parseAddonMetadata(jsonText)
                addonId = metadata.id

                try {
                    val clazz = Class.forName(
                        metadata.entrypoints.firstOrNull() ?: metadata.main(),
                        true,
                        Thread.currentThread().contextClassLoader
                    )
                    val instance = try {
                        val ctor = clazz.getDeclaredConstructor()
                        ctor.isAccessible = true
                        ctor.newInstance()
                    } catch (e: NoSuchMethodException) {
                        clazz.getField("INSTANCE").get(null)
                    }

                    val method = clazz.methods.find { it.name in listOf("onInitialize") }
                    method?.invoke(instance)

                    loadedEntrypoints.add(instance)
                    println("initialized addon entrypoint: ${clazz.name}")
                } catch (e: Exception) {
                    println("failed to initialize main class for ${metadata.name} - ${e.message}")
                    e.printStackTrace()
                }

                val classCount = loadAllClassesFromJar(jar)
                println("loaded $classCount classes from ${jarPath.fileName}")
            }
        } catch (e: Exception) {
            println("error parsing addon metadata for ${jarPath.fileName} - ${e.message}")
            e.printStackTrace()
        }

        return Pair(addonId, loadedEntrypoints)
    }

    private fun AddonMetadata.main(): String {
        return entrypoints.firstOrNull() ?: classes.firstOrNull() ?: ""
    }

    private fun parseAddonMetadata(json: String): AddonMetadata {
        return try {
            val map = mutableMapOf<String, Any>()
            val content = json.trim().removePrefix("{").removeSuffix("}")
            var inString = false
            var inArray = false
            var currentKey = ""
            var currentValue = StringBuilder()
            var arrayDepth = 0

            var i = 0
            while (i < content.length) {
                val c = content[i]
                when {
                    c == '"' && (i == 0 || content[i - 1] != '\\') -> {
                        inString = !inString
                        if (!inString && currentKey.isEmpty()) {
                            currentKey = currentValue.toString()
                            currentValue.clear()
                        }
                    }
                    c == '[' && !inString -> {
                        inArray = true
                        arrayDepth++
                    }
                    c == ']' && !inString -> {
                        arrayDepth--
                        if (arrayDepth == 0) {
                            inArray = false
                            val arrayContent = currentValue.toString()
                            val items = arrayContent.split(",")
                                .map { it.trim().removeSurrounding("\"") }
                                .filter { it.isNotEmpty() }
                            map[currentKey] = items
                            currentKey = ""
                            currentValue.clear()
                        }
                    }
                    c == ',' && !inString && !inArray -> {
                        if (currentKey.isNotEmpty() && currentValue.isNotEmpty()) {
                            map[currentKey] = currentValue.toString().trim().removeSurrounding("\"")
                            currentKey = ""
                            currentValue.clear()
                        }
                    }
                    c == ':' && !inString && !inArray -> Unit
                    !c.isWhitespace() || inString || inArray -> currentValue.append(c)
                }
                i++
            }

            if (currentKey.isNotEmpty() && currentValue.isNotEmpty())
                map[currentKey] = currentValue.toString().trim().removeSurrounding("\"")

            @Suppress("UNCHECKED_CAST")
            AddonMetadata(
                id = map["id"] as? String ?: "unknown",
                name = map["name"] as? String ?: "Unknown Addon",
                version = map["version"] as? String ?: "1.0.0",
                author = map["author"] as? String ?: "Unknown",
                description = map["description"] as? String ?: "",
                entrypoints = (map["entrypoints"] as? List<String>) ?: emptyList(),
                classes = (map["classes"] as? List<String>) ?: emptyList(),
                dependencies = (map["dependencies"] as? List<String>) ?: emptyList()
            )
        } catch (e: Exception) {
            println("Failed to parse cobalt.addon.json - ${e.message}")
            e.printStackTrace()
            AddonMetadata("unknown", "Unknown Addon", "1.0.0")
        }
    }

    private fun loadAllClassesFromJar(jar: JarFile): Int {
        var count = 0
        jar.entries().asSequence()
            .filter { it.name.endsWith(".class") && !it.isDirectory }
            .forEach { _ ->
                count++
            }
        return count
    }

    fun reload() {
        val startTime = System.currentTimeMillis()
        ChatUtils.sendMessage("Reloading!")
        ModuleManager.clearModules()
        
        val currentJars = addonsDir.listDirectoryEntries("*.jar").toSet()
        val previousJars = loadedAddons.keys.toSet()
        val removedJars = previousJars - currentJars

        currentJars.forEach { jarPath ->
            loadExternalAddon(jarPath)
        }

        removedJars.forEach { jarPath ->
            ChatUtils.sendDebug("Addon removed: ${jarPath.fileName}")
            loadedAddons.remove(jarPath)  
        }
        
        val endTime = System.currentTimeMillis()
        ChatUtils.sendMessage("Reload complete. Active addons: ${getActiveAddonCount()} - took ${endTime - startTime}ms")
        ConfigScreen.onReload()
    }

    fun getLoadedAddons(): List<AddonInfo> = loadedAddons.values.toList()
    fun getActiveAddonCount(): Int = loadedAddons.values.count { it.loaded } + activeAddons.size
    fun getAddons(): List<Addon> = activeAddons.toList()
}
