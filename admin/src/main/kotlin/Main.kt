import gui.AdminMainWindow
import gui.utils.GuiUtils
import minecraftAssetProviders.curseForge.data.CurseForgeDataSource
import minecraftAssetProviders.curseForge.data.RemoteCurseForgeDataSource
import javax.swing.SwingUtilities

val curseForgeDataSource: CurseForgeDataSource = RemoteCurseForgeDataSource()

fun main() {
    GuiUtils.setupSwingGui()
    GuiUtils.applyThemeIfNeeded(null, null)

    SwingUtilities.invokeLater {
        AdminMainWindow().isVisible = true
    }
}
