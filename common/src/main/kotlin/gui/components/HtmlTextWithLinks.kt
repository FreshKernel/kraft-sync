package gui.components

import gui.utils.GuiUtils
import utils.HtmlBuilder
import utils.SystemInfoProvider
import java.awt.Desktop
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent

class HtmlTextWithLinks(htmlBuilderBlock: HtmlBuilder.() -> Unit) :
    JEditorPane("text/html", HtmlBuilder().apply(htmlBuilderBlock).buildBodyAsText()) {
    init {
        addHyperlinkListener { e ->
            if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                if (!Desktop.isDesktopSupported()) {
                    GuiUtils.showErrorMessage(
                        title = "Unsupported Operation",
                        message = "⚠ Your operating system (${SystemInfoProvider.getOperatingSystemName()}) doesn't support opening links.",
                        parentComponent = this,
                    )
                    return@addHyperlinkListener
                }
                Desktop.getDesktop().browse(e.url.toURI())
            }
        }
        isEditable = false
        border = null
    }
}
