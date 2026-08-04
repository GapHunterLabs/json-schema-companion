package dev.gaphunter.jsonschemacompanion.reference

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Real PSI resolution across 2+ fixture files -- confirms `$ref` resolves
 * locally (same-file and cross-file), that a genuinely missing target
 * resolves to null instead of throwing, that an http(s) `$ref` never
 * resolves (the direct regression test for the cited "tries to download
 * them from the URI" competitor bug), and that files without a real
 * `"$schema"` never get OUR reference attached at all (the "randomly
 * recognizes JSON schemas" competitor bug).
 *
 * Assertions filter to [JsonRefReference] specifically, not the combined
 * `PsiElement.references` list -- the bundled JSON Schema support also
 * attaches its own reference to `$ref` values (confirmed: it resolves
 * loosely to the containing object even for a pointer with no matching
 * property), which is outside this plugin's control and not what these
 * tests are meant to verify.
 */
class JsonRefReferenceTest : BasePlatformTestCase() {

    private fun findRefStringLiteral(file: PsiFile): JsonStringLiteral {
        val jsonFile = file as JsonFile
        return PsiTreeUtil.findChildrenOfType(jsonFile, JsonStringLiteral::class.java)
            .first { literal ->
                val property = literal.parent as? JsonProperty
                property?.name == "\$ref" && property.value === literal
            }
    }

    private fun ourReference(literal: JsonStringLiteral): JsonRefReference? =
        literal.references.filterIsInstance<JsonRefReference>().firstOrNull()

    fun testResolvesSameFilePointerToADefinition() {
        myFixture.configureByText(
            "schema.json",
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "type": "object",
              "properties": {
                "user": { "${'$'}ref": "#/definitions/User" }
              },
              "definitions": {
                "User": { "type": "object" }
              }
            }
            """.trimIndent(),
        )
        val refLiteral = findRefStringLiteral(myFixture.file)
        val resolved = ourReference(refLiteral)?.resolve()
        assertNotNull("expected the \$ref to resolve", resolved)
        val property = resolved!!.parent as JsonProperty
        assertEquals("User", property.name)
    }

    fun testResolvesRefAcrossTwoFiles() {
        myFixture.addFileToProject(
            "definitions.json",
            """
            {
              "Address": { "type": "object" }
            }
            """.trimIndent(),
        )
        myFixture.configureByText(
            "schema.json",
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "properties": {
                "address": { "${'$'}ref": "definitions.json#/Address" }
              }
            }
            """.trimIndent(),
        )
        val refLiteral = findRefStringLiteral(myFixture.file)
        val resolved = ourReference(refLiteral)?.resolve()
        assertNotNull("expected the cross-file \$ref to resolve", resolved)
        val property = resolved!!.parent as JsonProperty
        assertEquals("Address", property.name)
        assertEquals("definitions.json", property.containingFile.name)
    }

    fun testDoesNotResolveWhenTargetIsMissing() {
        myFixture.configureByText(
            "schema.json",
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "properties": {
                "user": { "${'$'}ref": "#/definitions/DoesNotExist" }
              },
              "definitions": {}
            }
            """.trimIndent(),
        )
        val refLiteral = findRefStringLiteral(myFixture.file)
        val reference = ourReference(refLiteral)
        assertNotNull("expected our reference to still be attached", reference)
        assertNull(reference!!.resolve())
    }

    fun testDoesNotAttachOurReferenceOutsideARecognizedSchemaFile() {
        myFixture.configureByText(
            "plain.json",
            """
            {
              "${'$'}ref": "#/definitions/User",
              "definitions": { "User": {} }
            }
            """.trimIndent(),
        )
        val refLiteral = findRefStringLiteral(myFixture.file)
        assertNull(ourReference(refLiteral))
    }

    fun testNeverResolvesAnHttpRef() {
        myFixture.configureByText(
            "schema.json",
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "properties": {
                "user": { "${'$'}ref": "https://example.com/schemas/user.json" }
              }
            }
            """.trimIndent(),
        )
        val refLiteral = findRefStringLiteral(myFixture.file)
        val reference = ourReference(refLiteral)
        assertNotNull(reference)
        assertNull("an http(s) \$ref must never resolve via network access", reference!!.resolve())
    }
}
