package gui.dialogs

import gui.GuiState
import gui.components.BaseJFrame
import gui.utils.column
import gui.utils.onClick
import gui.utils.padding
import utils.terminateWithOrWithoutError
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JProgressBar

// TODO: Use onClose to close the file downloading process, might refactor this class to be subclass of JDialog
class LoadingIndicatorDialog(
    title: String,
    private val onClose: () -> Unit = {},
) : BaseJFrame() {
    private val infoLabel: JLabel = JLabel("In progress")
    private val progressBar: JProgressBar = JProgressBar(0, 100)
    private val detailsLabel: JLabel = JLabel("Loading...")

    companion object {
        fun createIfGuiEnabled(
            title: String,
            onClose: () -> Unit = {},
        ): LoadingIndicatorDialog? =
            if (GuiState.isGuiEnabled) {
                LoadingIndicatorDialog(
                    title = title,
                    onClose = onClose,
                )
            } else {
                null
            }
    }

    init {
        this.title = title
        minimumSize = Dimension(250, 155)
        maximumSize = Dimension(800, 400)
        defaultCloseOperation = EXIT_ON_CLOSE

        addWindowListener(
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    super.windowClosing(e)
                    closeDialog()
                }
            },
        )

        contentPane = getContent()
        pack()
        setLocationRelativeTo(null)
    }

    private fun getContent(): JComponent =
        column(
            infoLabel.padding(top = 12),
            progressBar.apply {
                preferredSize = Dimension(200, 20)
                padding(right = 12, left = 12, top = 12)
            },
            detailsLabel.apply {
                padding(top = 12, bottom = 12)
            },
            JButton("Cancel").onClick {
                closeDialog()
            },
        )

    private fun closeDialog() {
        onClose()
        terminateWithOrWithoutError()
    }

    fun updateComponentProperties(
        title: String?,
        infoText: String,
        progress: Int,
        detailsText: String,
    ) {
        title?.let { this.title = it }
        infoLabel.text = infoText
        progressBar.value = progress
        detailsLabel.text = detailsText
    }
}
