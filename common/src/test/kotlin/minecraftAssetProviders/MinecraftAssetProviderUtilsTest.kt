package minecraftAssetProviders

import kotlin.test.Test
import kotlin.test.assertIs

class MinecraftAssetProviderUtilsTest {
    @Test
    fun `test getAssetProvider with Modrinth assets source`() {
        assertIs<MinecraftAssetProvider.Modrinth>(
            MinecraftAssetProviderUtils.getAssetProvider(
                "https://cdn.modrinth.com/data/8oi3bsk5/versions/owDSctxm/Terralith_1.20_v2.5.0.jar",
            ).getOrThrow(),
        )
    }

    @Test
    fun `test getAssetProvider with CurseForge assets source`() {
        assertIs<MinecraftAssetProvider.CurseForge>(
            MinecraftAssetProviderUtils.getAssetProvider("https://edge.forgecdn.net/files/4576/4/eatinganimation-1.20-5.0.0.jar")
                .getOrThrow(),
        )
        assertIs<MinecraftAssetProvider.CurseForge>(
            MinecraftAssetProviderUtils.getAssetProvider(
                "https://mediafilez.forgecdn.net/files/5284/882/sodium-fabric-0.5.8%2Bmc1.20.5.jar",
            )
                .getOrThrow(),
        )
    }

    @Test
    fun `test getAssetProvider with Unknown assets source`() {
        assertIs<MinecraftAssetProvider.Unknown>(
            MinecraftAssetProviderUtils.getAssetProvider(
                "https://raw.githubusercontent.com/repositoryOrgnazation/repository/main/BOMD-1.8.2-1.20.4.jar",
            ).getOrThrow(),
        )
    }
}
