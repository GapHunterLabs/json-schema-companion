package dev.gaphunter.jsonschemacompanion.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefinitionsDefsMismatchTest {

    @Test
    fun `a definitions pointer's alternate is the equivalent defs pointer`() {
        assertEquals("/\$defs/Pet", DefinitionsDefsMismatch.alternatePointer("/definitions/Pet"))
    }

    @Test
    fun `a defs pointer's alternate is the equivalent definitions pointer`() {
        assertEquals("/definitions/Pet", DefinitionsDefsMismatch.alternatePointer("/\$defs/Pet"))
    }

    @Test
    fun `a nested path under definitions keeps its remaining segments in the alternate`() {
        assertEquals("/\$defs/Pet/properties/name", DefinitionsDefsMismatch.alternatePointer("/definitions/Pet/properties/name"))
    }

    @Test
    fun `an unrelated pointer shape has no alternate at all`() {
        assertNull(DefinitionsDefsMismatch.alternatePointer("/properties/name"))
    }

    @Test
    fun `describe names the keyword actually used in the document and the fixed ref`() {
        val message = DefinitionsDefsMismatch.describe("/definitions/Pet", "/\$defs/Pet")
        assertEquals("this document defines it under '\$defs', not 'definitions' -- try '#/\$defs/Pet'", message)
    }

    @Test
    fun `describe in the other direction names definitions as the real keyword`() {
        val message = DefinitionsDefsMismatch.describe("/\$defs/Pet", "/definitions/Pet")
        assertEquals("this document defines it under 'definitions', not '\$defs' -- try '#/definitions/Pet'", message)
    }
}
