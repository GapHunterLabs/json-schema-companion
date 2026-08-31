package dev.gaphunter.jsonschemacompanion.highlighting

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/** Real PSI, through the full extension pipeline -- same discipline as jenkinsfile-companion's/xsd-companion's own annotator tests. */
class JsonRefAnnotatorTest : BasePlatformTestCase() {

    private fun warnings(): List<String> =
        myFixture.doHighlighting()
            .filter { it.description != null }
            .map { it.description!! }

    fun testARefUnderDefsButDeclaredUnderDefinitionsGetsTheSpecificHint() {
        myFixture.configureByText(
            "schema.json",
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "definitions": { "Pet": { "type": "object" } },
              "properties": { "pet": { "${'$'}ref": "#/${'$'}defs/Pet" } }
            }
            """.trimIndent(),
        )
        assertTrue(
            "expected the definitions/\$defs mismatch hint, got: ${warnings()}",
            warnings().any { it.contains("defines it under 'definitions'") && it.contains("#/definitions/Pet") },
        )
    }

    fun testARefUnderDefinitionsButDeclaredUnderDefsGetsTheSpecificHint() {
        myFixture.configureByText(
            "schema.json",
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "${'$'}defs": { "Pet": { "type": "object" } },
              "properties": { "pet": { "${'$'}ref": "#/definitions/Pet" } }
            }
            """.trimIndent(),
        )
        assertTrue(
            "expected the \$defs/definitions mismatch hint, got: ${warnings()}",
            warnings().any { it.contains("defines it under '\$defs'") && it.contains("#/\$defs/Pet") },
        )
    }

    fun testAGenuinelyMissingDefinitionGetsNoMisleadingHint() {
        myFixture.configureByText(
            "schema.json",
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "definitions": { "Dog": { "type": "object" } },
              "properties": { "pet": { "${'$'}ref": "#/definitions/Cat" } }
            }
            """.trimIndent(),
        )
        val relevant = warnings().filter { it.contains("Cannot resolve reference") }
        assertEquals(1, relevant.size)
        assertTrue("did not expect a definitions/\$defs hint here, got: $relevant", relevant.none { it.contains("defines it under") })
    }
}
