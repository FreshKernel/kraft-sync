package util.gui

import syncInfo
import util.terminateWithError
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class LoadingIndicatorWindow(title: String) : JFrame() {
    private val frame: JFrame = JFrame(title)
    private val titleLabel: JLabel
    private val progressBar: JProgressBar
    private val infoLabel: JLabel

    init {
        val windowSize = Dimension(240, 150)
        frame.size = windowSize
        frame.minimumSize = windowSize
        // TODO: I might add cancel button too, also the close operation should make use of [ScriptConfig.launchOnError]
        frame.defaultCloseOperation =
            if (syncInfo.canCloseWhileDownloading) WindowConstants.EXIT_ON_CLOSE else WindowConstants.DO_NOTHING_ON_CLOSE
        if (syncInfo.canCloseWhileDownloading) {
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    println("0 = ${0}")
                    super.windowClosing(e)
                    println("3 = ${3}")
                }

                override fun windowClosed(e: WindowEvent?) {
                    println("1 = ${1}")
                    super.windowClosed(e)
                    println("2 = ${2}")
                }
            })
        }
        frame.iconImage = GuiUtils.getImageIcon().image

        val panel = JPanel()
        panel.layout = null

        titleLabel = JLabel("Please wait")
        titleLabel.setBounds(10, 10, 200, 20)
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER)

        progressBar = JProgressBar(0, 100)
        progressBar.setBounds(10, 40, 200, 20)
        progressBar.preferredSize = Dimension(200, 20)

        infoLabel = JLabel("Loading...")
        infoLabel.setBounds(10, 70, 200, 20)
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER)

        panel.add(titleLabel)
        panel.add(progressBar)
        panel.add(infoLabel)

        frame.add(panel)
        frame.setLocationRelativeTo(null)
    }

    fun showWindow() {
        frame.isVisible = true
    }

    fun hideWindow() {
        frame.isVisible = false
    }

    fun updateProgress(
        titleLabelText: String,
        progress: Float,
        infoLabelText: String,
        title: String? = null,
    ) {
        titleLabel.text = titleLabelText
        progressBar.value = progress.toInt()
        infoLabel.text = infoLabelText
        title?.let { frame.title = it }
    }
}
