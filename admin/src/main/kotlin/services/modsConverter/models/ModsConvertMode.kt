package services.modsConverter.models

enum class ModsConvertMode {
    InsideSyncInfo,
    AsModsList,
    ;

    override fun toString(): String {
        return when (this) {
            InsideSyncInfo -> "Convert as Sync Info"
            AsModsList -> "Convert as Mods List (Directly)"
        }
    }
}
