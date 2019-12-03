package utils

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils

fun ASTNode.line(inFile: PsiFile): Int {
    return DiagnosticUtils.getLineAndColumnInPsiFile(inFile, this.textRange).line
}