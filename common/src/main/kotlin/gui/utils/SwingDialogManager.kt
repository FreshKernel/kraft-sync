package gui.utils

import java.awt.Component
import javax.swing.Icon
import javax.swing.JOptionPane

object SwingDialogManager {
    enum class MessageType(val swingMessageType: Int) {
        Error(JOptionPane.ERROR_MESSAGE),
        Info(JOptionPane.INFORMATION_MESSAGE),
        Warning(JOptionPane.WARNING_MESSAGE),
        Question(JOptionPane.QUESTION_MESSAGE),
        Plain(JOptionPane.PLAIN_MESSAGE),
    }

    enum class ConfirmDialogOptionType(val swingOptionType: Int) {
        YesNo(JOptionPane.YES_NO_OPTION),
        YesNoCancel(JOptionPane.YES_NO_CANCEL_OPTION),
        OkCancel(JOptionPane.OK_CANCEL_OPTION),
    }

    enum class ConfirmDialogOptionTypeResult(val swingSelectedOption: Int) {
        Yes(0),
        Ok(0),
        No(1),
        Cancel(2),
        ;

        fun isConfirmed(): Boolean = this == Yes || this == Ok
    }

    fun showConfirmDialog(
        title: String,
        message: Any,
        parentComponent: Component?,
        optionType: ConfirmDialogOptionType = ConfirmDialogOptionType.YesNo,
        messageType: MessageType = MessageType.Info,
        icon: Icon? = null,
    ): ConfirmDialogOptionTypeResult {
        val result =
            JOptionPane.showConfirmDialog(
                parentComponent,
                message,
                title,
                optionType.swingOptionType,
                messageType.swingMessageType,
                icon,
            )
        when (optionType) {
            ConfirmDialogOptionType.YesNo -> {
                if (result == ConfirmDialogOptionTypeResult.Yes.swingSelectedOption) {
                    return ConfirmDialogOptionTypeResult.Yes
                }
                return ConfirmDialogOptionTypeResult.No
            }

            ConfirmDialogOptionType.YesNoCancel -> {
                if (result == ConfirmDialogOptionTypeResult.Yes.swingSelectedOption) {
                    return ConfirmDialogOptionTypeResult.Yes
                }
                if (result == ConfirmDialogOptionTypeResult.No.swingSelectedOption) {
                    return ConfirmDialogOptionTypeResult.No
                }
                return ConfirmDialogOptionTypeResult.Cancel
            }

            ConfirmDialogOptionType.OkCancel -> {
                if (result == ConfirmDialogOptionTypeResult.Ok.swingSelectedOption) {
                    return ConfirmDialogOptionTypeResult.Ok
                }
                return ConfirmDialogOptionTypeResult.Cancel
            }
        }
    }

    fun showMessageDialog(
        title: String,
        message: Any,
        parentComponent: Component?,
        messageType: MessageType = MessageType.Info,
        icon: Icon? = null,
    ) {
        JOptionPane.showMessageDialog(
            parentComponent,
            message,
            title,
            messageType.swingMessageType,
            icon,
        )
    }

    /**
     * @return null if the user canceled the input
     * */
    fun showInputDialog(
        title: String,
        message: Any,
        parentComponent: Component?,
        messageType: MessageType = MessageType.Info,
        icon: Icon? = null,
        selectionValues: Array<Any>?,
        initialSelectionValue: Any?,
    ): String? {
        return JOptionPane.showInputDialog(
            parentComponent,
            message,
            title,
            messageType.swingMessageType,
            icon,
            selectionValues,
            initialSelectionValue,
        ) as String?
    }
}
