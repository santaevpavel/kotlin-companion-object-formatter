package ru.santaev.companionObjectFormatter

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import ru.santaev.companionObjectFormatter.utils.line
import kotlin.math.max

class KtElementBoundFinder {

    fun find(ktFile: KtFile, ktElement: KtElement): Bounds {
        var lastElementLine = ktElement.node.line(ktFile)
        val psiElementVisitor = object : PsiElementVisitor() {

            override fun visitElement(element: PsiElement) {
                lastElementLine = max(lastElementLine, element.node.line(ktFile))
                super.visitElement(element)
                element.acceptChildren(this)
            }
        }
        ktElement.acceptChildren(psiElementVisitor)
        return Bounds(
            startLine = ktElement.node.line(ktFile),
            endLine = lastElementLine
        )
    }

    data class Bounds(val startLine: Int, val endLine: Int) {
        val range = IntRange(startLine, endLine)
    }
}
