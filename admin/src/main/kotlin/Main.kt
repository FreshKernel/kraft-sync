import gui.AdminMainWindow
import gui.utils.GuiUtils
import minecraftAssetProviders.curseForge.data.CurseForgeDataSource
import minecraftAssetProviders.curseForge.data.RemoteCurseForgeDataSource
import utils.HttpService
import javax.swing.SwingUtilities

val curseForgeDataSource: CurseForgeDataSource = RemoteCurseForgeDataSource(HttpService.client)

fun main() {
    GuiUtils.setupSwingGui()
    GuiUtils.applyThemeIfNeeded(null, null)

    SwingUtilities.invokeLater {
        AdminMainWindow().isVisible = true
    }
}
