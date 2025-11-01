package org.cobalt.api.util

import kotlin.math.ceil
import net.minecraft.util.math.BlockPos
import org.cobalt.CoreMod.mc

object PlayerUtils {

  val position: BlockPos
    get() = BlockPos.ofFloored(
      mc.player!!.x,
      ceil(mc.player!!.y) - 1,
      mc.player!!.z
    )

  val fov: Int
    get() = mc.options.fov.value

}
