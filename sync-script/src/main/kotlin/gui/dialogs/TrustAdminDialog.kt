package gui.dialogs

import config.models.ScriptConfig
import constants.ProjectInfoConstants
import gui.components.HtmlTextWithLinks
import gui.utils.SwingDialogManager
import minecraftAssetProviders.MinecraftAssetProviderUtils
import syncInfo.models.SyncInfo
import syncInfo.models.instance

object TrustAdminDialog {
    /**
     * @return If the user chose to trust the administration
     * */
    fun showDialog(): Boolean {
        val assetsProviders = MinecraftAssetProviderUtils.getAssetsProviders(SyncInfo.instance).getOrThrow()
        val result =
            SwingDialogManager.showConfirmDialog(
                title = "Security Notice",
                message =
                    HtmlTextWithLinks {
                        text("It appears this is your first time using ")
                        link(ProjectInfoConstants.DISPLAY_NAME, ProjectInfoConstants.WEBSITE)
                        text(" from this ")
                        link("source", ScriptConfig.getInstanceOrThrow().syncInfoUrl)
                        newLine()

                        text("Although ")
                        link(ProjectInfoConstants.DISPLAY_NAME, ProjectInfoConstants.REPOSITORY_LINK)
                        text(" is secure and open-source, be cautious")
                        newLine()

                        text("about the sources you download assets from, as they might not always be safe.")
                        newLine()

                        text("The used asset providers are: ")
                        assetsProviders.forEach {
                            link(
                                labelText = it.providerName,
                                linkUrl = it.link,
                            )
                            if (it != assetsProviders.last()) {
                                text(", ")
                            }
                        }
                        newLine()

                        text("It's important to notice that the assets providers mentioned above ")
                        newLine()
                        text("and the assets can be changed by the administration at anytime.")
                        newLines(2)

                        text("Can we count on your trust in the administration regarding this matter?")
                    },
                parentComponent = null,
                optionType = SwingDialogManager.ConfirmDialogOptionType.YesNo,
                messageType =
                    if (assetsProviders.any { !it.isKnownProvider }) {
                        SwingDialogManager.MessageType.Warning
                    } else {
                        SwingDialogManager.MessageType.Info
                    },
            )
        return result.isConfirmed()
    }
}
