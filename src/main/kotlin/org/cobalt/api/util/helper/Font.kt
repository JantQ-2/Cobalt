package org.cobalt.api.util.helper

import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Implementation from OdinFabric by odtheking
 * Original work: https://github.com/odtheking/OdinFabric
 *
 * @author Odin Contributors
 */
class Font(val name: String, private val resourcePath: String) {

  private val cachedBytes: ByteArray? = null

  fun buffer(): ByteBuffer {
    val bytes = cachedBytes ?: run {
      val stream = this::class.java.getResourceAsStream(resourcePath) ?: throw FileNotFoundException(resourcePath)
      stream.use { it.readBytes() }
    }

    return ByteBuffer.allocateDirect(bytes.size)
      .order(ByteOrder.nativeOrder())
      .put(bytes)
      .flip() as ByteBuffer
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    return other is Font && name == other.name
  }

}
