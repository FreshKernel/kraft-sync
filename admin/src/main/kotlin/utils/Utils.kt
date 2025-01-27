package utils

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import kotlinx.serialization.json.JsonObject
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun String.copyToClipboard() {
    Toolkit
        .getDefaultToolkit()
        .systemClipboard
        .setContents(StringSelection(this), null)
}

/**
 * Merge [original] and [updates], will overwrite [original].
 * If there is a nested object inside it, and all the properties are available
 * in [original] and not in [updates] as the [updates] only have the changes.
 *
 * In that case, the properties of that nested object will be removed inside the [original] and replaced with
 * the nested object in the [updates].
 *
 * */
fun simpleMergeJsonObjects(
    original: JsonObject,
    updates: JsonObject,
): JsonObject = JsonObject(original + updates)

val TomlIgnoreUnknownKeys = Toml(inputConfig = TomlInputConfig(ignoreUnknownNames = true))
