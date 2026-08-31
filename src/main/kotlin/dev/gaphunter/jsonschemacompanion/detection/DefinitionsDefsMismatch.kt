package dev.gaphunter.jsonschemacompanion.detection

/**
 * Flags the specific, real mistake of a `$ref` written for the *other*
 * reusable-schema keyword: JSON Schema Draft 2019-09 introduced
 * `$defs` as the new conventional location for reusable definitions,
 * replacing `definitions` (both remain technically valid JSON --
 * neither is forbidden -- but a schema only ever declares its shared
 * types under one of the two in practice, so a `$ref` using the other
 * one silently fails to resolve). Common after copy-pasting a `$ref`
 * from an older/newer schema, or migrating a schema between drafts
 * without updating every reference.
 *
 * Pure shape logic only -- whether the *alternate* pointer actually
 * resolves in the real document is checked by the caller (PSI access
 * lives in the annotator, not here), so this never guesses.
 */
object DefinitionsDefsMismatch {

    private const val DEFINITIONS_PREFIX = "/definitions/"
    private const val DEFS_PREFIX = "/\$defs/"

    /**
     * [pointer] is the fragment after `#`, as already decoded. Returns
     * the alternate pointer to try, or null when this pointer doesn't
     * even have the shape this check cares about (some other path
     * entirely, or a bare document reference with no fragment).
     */
    fun alternatePointer(pointer: String): String? = when {
        pointer.startsWith(DEFINITIONS_PREFIX) -> DEFS_PREFIX + pointer.removePrefix(DEFINITIONS_PREFIX)
        pointer.startsWith(DEFS_PREFIX) -> DEFINITIONS_PREFIX + pointer.removePrefix(DEFS_PREFIX)
        else -> null
    }

    /** Human-readable explanation once the caller has confirmed [alternatePointer] really does resolve. */
    fun describe(pointer: String, alternate: String): String {
        val (usedWrong, usedRight) = if (pointer.startsWith(DEFINITIONS_PREFIX)) {
            "definitions" to "\$defs"
        } else {
            "\$defs" to "definitions"
        }
        return "this document defines it under '$usedRight', not '$usedWrong' -- try '#$alternate'"
    }
}
