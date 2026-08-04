package dev.gaphunter.jsonschemacompanion.pointer

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonValue
import com.intellij.psi.PsiElement

/**
 * RFC 6901 JSON Pointer resolution against real JSON PSI -- entirely
 * local tree navigation, no network I/O, no external database. This is
 * the direct fix for the cited competitor complaint: "the plugin seems
 * not to be able to resolve the references via an internal database but
 * tries to download them from the URI used as ID. This makes references
 * unusable in local development."
 */
object JsonPointer {
    /** [pointer] is the fragment after `#`, e.g. `/definitions/Foo/properties/bar`
     * (RFC 6901; empty string means "the document root"). Resolving through
     * an object property lands on that property's name element (its
     * declaration), matching ordinary "go to declaration" behavior. */
    fun resolve(root: JsonValue, pointer: String): PsiElement? {
        if (pointer.isEmpty()) return root
        if (!pointer.startsWith("/")) return null

        var current: JsonValue = root
        val segments = pointer.substring(1).split("/")
        for ((index, rawSegment) in segments.withIndex()) {
            val segment = unescape(rawSegment)
            val isLast = index == segments.lastIndex
            when (val node = current) {
                is JsonObject -> {
                    val property = node.findProperty(segment) ?: return null
                    if (isLast) return property.nameElement
                    current = property.value ?: return null
                }
                is JsonArray -> {
                    val itemIndex = segment.toIntOrNull() ?: return null
                    val items = node.valueList
                    if (itemIndex !in items.indices) return null
                    val value = items[itemIndex]
                    if (isLast) return value
                    current = value
                }
                else -> return null
            }
        }
        return current
    }

    /** Per RFC 6901: `~1` -> `/` must be applied before `~0` -> `~`. */
    private fun unescape(segment: String): String = segment.replace("~1", "/").replace("~0", "~")
}
