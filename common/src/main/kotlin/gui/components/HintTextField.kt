package gui.components

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JTextField

/**
 * @author Adam Gawne-Cain
 * */
class HintTextField(private val hintText: String) : JTextField() {
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (text.isEmpty()) {
            val graphics2D = g as? Graphics2D ?: return
            graphics2D.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
            )
            val fontMetrics = graphics2D.fontMetrics
            val textHeight = fontMetrics.ascent
            val x = insets.left
            val y = (height - textHeight) / 2 + fontMetrics.ascent
            val c0 = background.rgb
            val c1 = foreground.rgb
            val c2 = ((c0 and -0x1010102) ushr 1) + ((c1 and -0x1010102) ushr 1)
            graphics2D.color = Color(c2, true)
            graphics2D.drawString(hintText, x, y)
        }
    }
}
