package gui.components

import gui.utils.GuiUtils
import utils.buildHtml
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JTextField

fun instanceDirectoryLabeledInput(
    textField: JTextField,
    parentComponent: JComponent,
    preferredLabelWidth: Int,
): JComponent =
    labeledInputPanel(
        labelText = "Instance directory",
        tooltipText =
            buildHtml {
                text("The launcher instance directory.")
                newLine()
                text("Usually it's same path when you click ")
                boldText("Open Instance Folder")
                text(" for the selected instance in the launcher.")
                newLines(2)
                text("If the root instance folder does not have Minecraft specific files and folders")
                newLine()
                text("like mods, options.txt, resourcepacks and others. Navigate to ")
                boldText(".minecraft")
                text(" folder which is usually inside the instance/profile folder.")
            }.buildBodyAsText(),
        inputComponent =
            filePicker(
                filePathTextField = textField,
                fileChooser =
                    JFileChooser().apply {
                        dialogTitle = "Choose the instance directory"
                        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    },
                onErrorWhileChoosingFile = {
                    GuiUtils.showErrorMessage(
                        title = "Unexpected Error",
                        message = "An error occurred while trying to pick the launcher instance directory.",
                        parentComponent = parentComponent,
                    )
                },
            ),
        preferredLabelWidth = preferredLabelWidth,
    )
