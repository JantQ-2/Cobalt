import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  id("fabric-loom")
  `maven-publish`
  java
}

val baseGroup: String by project
val lwjglVersion: String by project
val modVersion: String by project
val modName: String by project

base {
  archivesName = modName
  version = modVersion
  group = baseGroup
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
  maven("https://api.modrinth.com/maven")
}

dependencies {
  minecraft("com.mojang:minecraft:${property("minecraft_version")}")
  mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
  modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

  modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
  modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

  modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

  modImplementation("org.lwjgl:lwjgl-nanovg:${lwjglVersion}")
  include("org.lwjgl:lwjgl-nanovg:${lwjglVersion}")

  listOf("windows", "linux", "macos", "macos-arm64").forEach {
    modImplementation("org.lwjgl:lwjgl-nanovg:${lwjglVersion}:natives-$it")
    include("org.lwjgl:lwjgl-nanovg:${lwjglVersion}:natives-$it")
  }

  modImplementation("com.github.caoimhebyrne:KDiscordIPC:0.2.3")
  include("com.github.caoimhebyrne:KDiscordIPC:0.2.3")
}

tasks {
  processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
      expand(getProperties())
      expand(mutableMapOf("version" to project.version))
    }
  }

  publishing {
    publications {
      create<MavenPublication>("mavenJava") {
        artifact(remapJar) {
          builtBy(remapJar)
        }

        artifact(kotlinSourcesJar) {
          builtBy(remapSourcesJar)
        }
      }
    }
  }

  compileKotlin {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
    }
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}
