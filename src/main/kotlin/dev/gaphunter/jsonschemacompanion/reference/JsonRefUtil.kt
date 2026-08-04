package dev.gaphunter.jsonschemacompanion.reference

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import dev.gaphunter.jsonschemacompanion.detection.JsonSchemaDetector

private const val REF_KEYWORD = "\$ref"

/** Shared "is this string literal a `$ref` VALUE inside a recognized JSON
 * Schema file" check -- used by both [JsonRefReferenceContributor] (to
 * decide whether to offer navigation) and `JsonRefAnnotator` (to decide
 * whether to check resolution), so the two never drift out of sync. */
object JsonRefUtil {
    fun asRefProperty(literal: JsonStringLiteral): JsonProperty? {
        if (literal.isPropertyName) return null
        val property = literal.parent as? JsonProperty ?: return null
        if (property.name != REF_KEYWORD) return null
        if (property.value !== literal) return null
        val containingFile = literal.containingFile as? JsonFile ?: return null
        if (!JsonSchemaDetector.isJsonSchemaFile(containingFile)) return null
        return property
    }
}
