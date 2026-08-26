package dev.gaphunter.jsonschemacompanion.highlighting

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import dev.gaphunter.jsonschemacompanion.reference.JsonRefReference
import dev.gaphunter.jsonschemacompanion.reference.JsonRefUtil
import dev.gaphunter.jsonschemacompanion.review.ReviewPrompt

/** Flags a `$ref` value that fails to resolve -- real, visible feedback
 * for a broken reference, on top of the go-to-definition navigation
 * [dev.gaphunter.jsonschemacompanion.reference.JsonRefReferenceContributor]
 * already provides for valid ones. */
class JsonRefAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val literal = element as? JsonStringLiteral ?: return
        JsonRefUtil.asRefProperty(literal) ?: return

        if (JsonRefReference(literal).resolve() == null) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Cannot resolve reference '${literal.value}'")
                .range(literal.textRange)
                .create()
            val file = literal.containingFile
            val lineNumber = file.viewProvider.document?.getLineNumber(literal.textRange.startOffset)?.plus(1) ?: 0
            ReviewPrompt.recordHit(file.project, "${file.virtualFile?.path}:$lineNumber:${literal.value}")
        }
    }
}
