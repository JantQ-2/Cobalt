package org.cobalt.internal.ui.component

internal abstract class Component {

  var x: Float = -1f
  var y: Float = -1f

  open fun draw(x: Float, y: Float) {
    this.x = x
    this.y = y
  }

  open fun onClick(button: Int): Boolean = false

}
