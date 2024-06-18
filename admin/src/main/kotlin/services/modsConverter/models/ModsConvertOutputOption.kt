package services.modsConverter.models

enum class ModsConvertOutputOption {
    CopyToClipboard,
    SaveAsFile,
    ;

    override fun toString(): String {
        return when (this) {
            CopyToClipboard -> "Copy to Clipboard"
            SaveAsFile -> "Save as File"
        }
    }
}
