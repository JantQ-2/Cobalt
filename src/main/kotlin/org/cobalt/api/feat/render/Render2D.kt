package org.cobalt.api.feat.render

import com.mojang.blaze3d.systems.RenderSystem
import java.awt.Color
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.cobalt.api.util.render.RenderLayers
import org.cobalt.api.util.render.WorldRenderContext
import org.joml.Vector3f

object Render2D {

  fun drawBox(context: WorldRenderContext, box: Box, color: Color, esp: Boolean = false) {
    val matrix = context.matrixStack ?: return
    val bufferSource = context.consumers as? VertexConsumerProvider.Immediate ?: return

    val r = color.red / 255f
    val g = color.green / 255f
    val b = color.blue / 255f

    val fillLayer = if (esp) RenderLayers.TRIANGLE_STRIP_ESP else RenderLayers.TRIANGLE_STRIP
    val lineLayer = if (esp) RenderLayers.LINE_LIST_ESP else RenderLayers.LINE_LIST

    matrix.push()
    with(context.camera.pos) { matrix.translate(-x, -y, -z) }

    VertexRendering.drawFilledBox(
      matrix,
      bufferSource.getBuffer(fillLayer),
      box.minX, box.minY, box.minZ,
      box.maxX, box.maxY, box.maxZ,
      r, g, b, 100 / 255F
    )

    VertexRendering.drawBox(
      matrix.peek(),
      bufferSource.getBuffer(lineLayer),
      box.minX, box.minY, box.minZ,
      box.maxX, box.maxY, box.maxZ,
      r, g, b, 1f
    )

    matrix.pop()
    bufferSource.draw(fillLayer)
    bufferSource.draw(lineLayer)
  }

  fun drawLine(
    context: WorldRenderContext,
    start: Vec3d,
    end: Vec3d,
    color: Color,
    esp: Boolean = false,
    thickness: Float = 2f,
  ) {
    val matrix = context.matrixStack ?: return
    val bufferSource = context.consumers as? VertexConsumerProvider.Immediate ?: return
    val layer = if (esp) RenderLayers.LINE_LIST_ESP else RenderLayers.LINE_LIST
    RenderSystem.lineWidth(thickness)

    matrix.push()
    with(context.camera.pos) { matrix.translate(-x, -y, -z) }

    val startOffset = Vector3f(start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
    val direction = end.subtract(start)

    VertexRendering.drawVector(
      matrix,
      bufferSource.getBuffer(layer),
      startOffset,
      direction,
      color.rgb
    )

    matrix.pop()
    bufferSource.draw(layer)
  }

}
