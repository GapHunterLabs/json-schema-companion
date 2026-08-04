package dev.gaphunter.jsonschemacompanion.detection

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral

/**
 * Recognizes a JSON file as a JSON Schema document by real content --
 * a top-level `"$schema"` property whose value references a real
 * json-schema.org meta-schema URI -- never by file extension/name
 * guessing. Directly answers the cited competitor complaint: "Activates
 * if you figure out the file extension it triggers on... the plugin
 * randomly recognizes JSON schemas."
 */
object JsonSchemaDetector {
    private const val SCHEMA_PROPERTY = "\$schema"

    fun isJsonSchemaFile(file: JsonFile): Boolean {
        val root = file.topLevelValue as? JsonObject ?: return false
        val schemaProperty = root.findProperty(SCHEMA_PROPERTY) ?: return false
        val value = schemaProperty.value as? JsonStringLiteral ?: return false
        return "json-schema.org" in value.value
    }
}
