package org.cobalt.api.util.render

import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack

class WorldRenderContext {
  var matrixStack: MatrixStack? = null
  lateinit var consumers: VertexConsumerProvider
  lateinit var camera: Camera
}
