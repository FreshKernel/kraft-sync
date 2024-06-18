package gui

import gui.utils.padding
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

abstract class Tab(
    private val topContentPadding: Int = 6,
    private val leftContentPadding: Int = 16,
    private val bottomContentPadding: Int = 6,
    private val rightContentPadding: Int = 16,
) : JPanel() {
    fun setupTabContent() {
        layout = BorderLayout()
        padding(topContentPadding, leftContentPadding, bottomContentPadding, rightContentPadding)

        this.add(this.getTabContent())
    }

    abstract fun getTabContent(): JComponent
}
