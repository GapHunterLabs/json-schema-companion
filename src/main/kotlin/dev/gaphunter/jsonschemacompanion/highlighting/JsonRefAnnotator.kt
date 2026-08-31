package dev.gaphunter.jsonschemacompanion.highlighting

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import dev.gaphunter.jsonschemacompanion.detection.DefinitionsDefsMismatch
import dev.gaphunter.jsonschemacompanion.pointer.JsonPointer
import dev.gaphunter.jsonschemacompanion.reference.JsonRefReference
import dev.gaphunter.jsonschemacompanion.reference.JsonRefUtil
import dev.gaphunter.jsonschemacompanion.review.ReviewPrompt
import java.net.URLDecoder

/** Flags a `$ref` value that fails to resolve -- real, visible feedback
 * for a broken reference, on top of the go-to-definition navigation
 * [dev.gaphunter.jsonschemacompanion.reference.JsonRefReferenceContributor]
 * already provides for valid ones. */
class JsonRefAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val literal = element as? JsonStringLiteral ?: return
        JsonRefUtil.asRefProperty(literal) ?: return

        if (JsonRefReference(literal).resolve() == null) {
            val hint = definitionsDefsHint(literal)
            val message = if (hint != null) "Cannot resolve reference '${literal.value}' -- $hint" else "Cannot resolve reference '${literal.value}'"
            holder.newAnnotation(HighlightSeverity.WARNING, message)
                .range(literal.textRange)
                .create()
            val file = literal.containingFile
            val lineNumber = file.viewProvider.document?.getLineNumber(literal.textRange.startOffset)?.plus(1) ?: 0
            ReviewPrompt.recordHit(file.project, "${file.virtualFile?.path}:$lineNumber:${literal.value}")
        }
    }

    /**
     * Re-parses the same `$ref` text [JsonRefReference] already tried
     * (file part + pointer part) and, only when the pointer looks like
     * the `definitions`/`$defs` mismatch shape, checks whether the
     * *alternate* keyword actually resolves against the real target
     * document -- never guesses, only speaks up when it has confirmed
     * the real fix.
     */
    private fun definitionsDefsHint(literal: JsonStringLiteral): String? {
        val refText = literal.value
        val hashIndex = refText.indexOf('#')
        if (hashIndex < 0) return null
        val filePart = refText.substring(0, hashIndex)
        val pointerPart = refText.substring(hashIndex + 1)
        val decodedPointer = try {
            URLDecoder.decode(pointerPart, "UTF-8")
        } catch (e: Exception) {
            pointerPart
        }
        val alternate = DefinitionsDefsMismatch.alternatePointer(decodedPointer) ?: return null

        val targetFile: JsonFile = if (filePart.isBlank()) {
            literal.containingFile as? JsonFile ?: return null
        } else {
            val baseDir = literal.containingFile?.originalFile?.virtualFile?.parent ?: return null
            val targetVirtualFile = VfsUtilCore.findRelativeFile(filePart, baseDir) ?: return null
            PsiManager.getInstance(literal.project).findFile(targetVirtualFile) as? JsonFile ?: return null
        }
        val root = targetFile.topLevelValue ?: return null
        if (JsonPointer.resolve(root, alternate) == null) return null

        return DefinitionsDefsMismatch.describe(decodedPointer, alternate)
    }
}
