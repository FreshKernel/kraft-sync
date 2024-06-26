package gui.utils

import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.border.Border

fun column(init: JPanel.() -> Unit): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
    panel.init()
    return panel
}

fun column(
    vararg components: JComponent,
    init: JPanel.() -> Unit = {},
): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
    components.forEach {
        it.alignmentX = JComponent.CENTER_ALIGNMENT
        panel.add(it)
    }
    panel.init()
    return panel
}

fun row(init: JPanel.() -> Unit): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
    panel.init()
    return panel
}

fun row(
    vararg components: JComponent,
    init: JPanel.() -> Unit = {},
): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
    components.forEach {
        it.alignmentX = JComponent.CENTER_ALIGNMENT
        panel.add(it)
    }
    panel.init()
    return panel
}

fun <T : JComponent> T.addComponentTo(parentComponent: JComponent): T {
    parentComponent.add(this)
    return this
}

fun <T : JComponent> T.addComponentTo(
    parentComponent: JComponent,
    constraints: Any,
): T {
    parentComponent.add(this, constraints)
    return this
}

fun <T : JComponent> T.padding(
    top: Int? = null,
    left: Int? = null,
    bottom: Int? = null,
    right: Int? = null,
): T {
    val currentBorder = this.border ?: BorderFactory.createEmptyBorder()

    val currentBorderInsets = currentBorder.getBorderInsets(this)
    val newTop = top ?: currentBorderInsets.top
    val newLeft = left ?: currentBorderInsets.left
    val newBottom = bottom ?: currentBorderInsets.bottom
    val newRight = right ?: currentBorderInsets.right

    this.border = BorderFactory.createEmptyBorder(newTop, newLeft, newBottom, newRight)
    return this
}

fun <T : JButton> T.onClick(onClick: (event: ActionEvent) -> Unit): T {
    addActionListener { event ->
        onClick(event)
    }
    return this
}

inline fun <reified T> JComboBox<T>.onItemSelected(crossinline onItemSelected: (item: T?, event: ItemEvent) -> Unit): JComboBox<T> {
    this.addItemListener { event ->
        if (event.stateChange != ItemEvent.SELECTED) {
            return@addItemListener
        }
        if (this.selectedItem == null || this.selectedItem !is T) {
            onItemSelected(null, event)
            return@addItemListener
        }
        onItemSelected(this.getSelectedItemOrThrow(), event)
    }
    return this
}

inline fun <reified T> JComboBox<T>.onItemChanged(crossinline onItemChanged: (item: T, event: ActionEvent) -> Unit): JComboBox<T> {
    this.addActionListener { event ->
        onItemChanged(this.getSelectedItemOrThrow(), event)
    }
    return this
}

/**
 *
 * @throws ClassCastException If [JComboBox.isEditable] is set to true, and the value doesn't match
 * the type of [T]
 *
 * @throws NullPointerException When calling [JComboBox.removeAllItems]
 * */
inline fun <reified T> JComboBox<T>.getSelectedItemOrThrow(): T =
    kotlin.runCatching { this.selectedItem as T }.getOrElse {
        if (it is ClassCastException) {
            throw ClassCastException("Can't cast the selected JComboBox item (${this.selectedItem}): ${it.message}")
        }
        if (it is NullPointerException) {
            throw NullPointerException("The selected JComboBox item is null: ${it.message}")
        }
        throw it
    }

/**
 * Same as [JComboBox.selectingItem] except will enforce the type safety when setting the new item.
 * @see getSelectedItemOrThrow
 * */
fun <T> JComboBox<T>.setSelectedItemSafe(newItem: T) {
    selectedItem = newItem
}

fun <T : JCheckBox> T.onValueChanged(onValueChanged: (newValue: Boolean, event: ItemEvent) -> Unit): T {
    addItemListener {
        onValueChanged(this.isSelected, it)
    }
    return this
}

fun JComponent.getCurrentWindowFrame(): JFrame = SwingUtilities.windowForComponent(this) as JFrame

fun JLabel.fontSize(value: Float): JLabel {
    font = font.deriveFont(value)
    return this
}

enum class FontStyle(
    val swingFontStyle: Int,
) {
    Plain(0),
    Bold(1),
    Italic(2),
}

fun <T : JLabel> T.fontStyle(fontStyle: FontStyle): T {
    font = font.deriveFont(fontStyle.swingFontStyle)
    return this
}

fun spacer(
    width: Int,
    height: Int,
): JComponent = Box.createRigidArea(Dimension(width, height)) as JComponent

fun <T : JComponent> T.backgroundColor(color: Color): T {
    background = color
    return this
}

fun <T : JComponent> T.border(border: Border): T {
    this.border = border
    return this
}
