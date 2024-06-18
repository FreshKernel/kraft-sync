package gui.components

import javax.swing.Icon
import javax.swing.JLabel

class LabeledIconWithTooltip(
    labelText: String,
    icon: Icon?,
    tooltipText: String,
) : JLabel(labelText) {
    init {
        super.setIcon(icon)
        super.setToolTipText(tooltipText)
    }
}

class LabelWithTooltip(
    labelText: String,
    tooltipText: String,
) : JLabel(labelText) {
    init {
        super.setToolTipText(tooltipText)
    }
}
