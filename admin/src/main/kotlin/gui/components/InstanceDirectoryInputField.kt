package gui.components

import gui.utils.ComboItem
import gui.utils.GuiUtils
import gui.utils.getSelectedItemOrThrow
import gui.utils.handleResult
import gui.utils.onClick
import gui.utils.onItemChanged
import gui.utils.onItemSelected
import gui.utils.row
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import launchers.Instance
import launchers.LauncherDataSource
import launchers.LauncherDataSourceFactory
import launchers.MinecraftLauncher
import utils.buildHtml
import java.awt.Component
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JTextField
import javax.swing.plaf.basic.BasicComboBoxEditor
import kotlin.io.path.absolutePathString

fun instanceDirectoryInputField(
    inputComboBox: JComboBox<ComboItem<Instance>>,
    launcherComboBox: JComboBox<MinecraftLauncher>,
    parentComponent: JComponent,
    preferredLabelWidth: Int,
): JComponent =
    labeledInputField(
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
                newLine()
                text("folder which is usually inside the instance/profile folder.")
                newLines(2)
                text(
                    "You can also choose the instance directly using the dropdown menu, this will only work",
                )
                newLine()
                text("if the selected launcher store the data in a known place and the launcher is installed on the system.")
            }.buildBodyAsText(),
        inputComponent =
            row(
                inputComboBox.apply {
                    isEditable = true

                    // Set custom editor to limit the width of the combo box text field
                    editor =
                        object : BasicComboBoxEditor() {
                            override fun getEditorComponent(): Component {
                                val editorComponent = super.getEditorComponent() as JTextField
                                editorComponent.preferredSize =
                                    Dimension(150, editorComponent.preferredSize.height)
                                editorComponent.toolTipText = editorComponent.text
                                return editorComponent
                            }
                        }

                    fun setDropdownItems() {
                        val launcherDataSource: LauncherDataSource? =
                            LauncherDataSourceFactory.getHandlerOrNull(launcherComboBox.getSelectedItemOrThrow())

                        removeAllItems()

                        CoroutineScope(Dispatchers.IO).launch {
                            launcherDataSource?.getInstances()?.getOrNull()?.forEach {
                                addItem(
                                    ComboItem(
                                        value = it,
                                        label = it.instanceName,
                                    ),
                                )
                            }
                        }
                    }
                    setDropdownItems()

                    launcherComboBox.onItemChanged { _, _ ->
                        setDropdownItems()
                    }

                    onItemSelected { item, _ ->
                        // Change the selected item value to the path as this will be handled as Text Field

                        if (item == null) {
                            // Calling removeAllItems() will update the selected item to be null
                            return@onItemSelected
                        }

                        // This will be triggered twice upon an item change, the first will be the item type,
                        // The second which is changed in this lambda, which is why we need to check to avoid
                        // unexpected error on the second change. The (item) will be null if the selected item
                        // is null or is different from the JComboBox<E> type of the elements
                        selectedItem = item.value.launcherInstanceDirectory.absolutePath
                    }
                },
                JButton("Browse").onClick {
                    val fileChooser =
                        JFileChooser().apply {
                            dialogTitle = "Choose the instance directory"
                            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                        }
                    val result = fileChooser.showOpenDialog(parentComponent)
                    val selectedFilePath =
                        fileChooser.handleResult(
                            result = result,
                            onErrorWhileChoosingFile = {
                                GuiUtils.showErrorMessage(
                                    title = "Unexpected Error",
                                    message = "An error occurred while trying to pick the launcher instance directory.",
                                    parentComponent = parentComponent,
                                )
                            },
                        ) ?: return@onClick
                    inputComboBox.selectedItem = selectedFilePath.absolutePathString()
                },
            ),
        preferredLabelWidth = preferredLabelWidth,
    )
