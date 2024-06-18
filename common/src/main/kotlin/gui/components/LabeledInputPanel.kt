package gui.components

import gui.utils.addComponentTo
import gui.utils.row
import java.awt.Dimension
import java.awt.GridBagConstraints
import javax.swing.BorderFactory
import javax.swing.JComponent

fun labeledInputPanel(
    labelText: String,
    tooltipText: String,
    inputComponent: JComponent,
    preferredLabelWidth: Int = 100,
): JComponent {
    val constraints = GridBagConstraints()
    return row {
        LabelWithTooltip(
            labelText = "$labelText:",
            tooltipText = tooltipText,
        ).apply {
            preferredSize = Dimension(preferredLabelWidth, this.preferredSize.height)
            constraints.gridx = 0
            constraints.gridy = 0
            constraints.anchor = GridBagConstraints.WEST
            labelFor = inputComponent
        }.addComponentTo(this@row, constraints)

        inputComponent
            .apply {
                maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
                constraints.gridx = 1
                constraints.weightx = 1.0
                constraints.fill = GridBagConstraints.HORIZONTAL
            }.addComponentTo(this@row, constraints)

        border = BorderFactory.createEmptyBorder(0, 0, 10, 0)
    }
}
