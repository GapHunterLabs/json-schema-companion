package dev.gaphunter.jsonschemacompanion.reference

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import dev.gaphunter.jsonschemacompanion.pointer.JsonPointer
import java.net.URLDecoder

/**
 * Resolves a `$ref` value (`"#/definitions/Foo"`, `"other.json"`,
 * `"./sub/other.json#/definitions/Foo"`) purely against local files --
 * [resolveLocalFile] only ever walks [VfsUtilCore.findRelativeFile]
 * relative to the referencing file's own directory. There is no HTTP
 * client anywhere in this plugin; a `$ref` using an `http(s)://` URI
 * simply fails to resolve (shown as unresolved, same as any other broken
 * reference) rather than attempting a network fetch.
 */
class JsonRefReference(element: JsonStringLiteral) :
    PsiReferenceBase<JsonStringLiteral>(element, ElementManipulators.getValueTextRange(element)) {

    override fun resolve(): PsiElement? {
        val refText = element.value
        val hashIndex = refText.indexOf('#')
        val filePart = if (hashIndex >= 0) refText.substring(0, hashIndex) else refText
        val pointerPart = if (hashIndex >= 0) refText.substring(hashIndex + 1) else ""

        val targetFile: JsonFile = if (filePart.isBlank()) {
            element.containingFile as? JsonFile ?: return null
        } else {
            resolveLocalFile(filePart) ?: return null
        }

        val decodedPointer = try {
            URLDecoder.decode(pointerPart, "UTF-8")
        } catch (e: Exception) {
            pointerPart
        }
        if (decodedPointer.isEmpty()) return targetFile.topLevelValue ?: targetFile

        val root = targetFile.topLevelValue ?: return null
        return JsonPointer.resolve(root, decodedPointer)
    }

    private fun resolveLocalFile(relativePath: String): JsonFile? {
        val currentVirtualFile = element.containingFile?.originalFile?.virtualFile ?: return null
        val baseDir = currentVirtualFile.parent ?: return null
        val targetVirtualFile = VfsUtilCore.findRelativeFile(relativePath, baseDir) ?: return null
        return PsiManager.getInstance(element.project).findFile(targetVirtualFile) as? JsonFile
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
