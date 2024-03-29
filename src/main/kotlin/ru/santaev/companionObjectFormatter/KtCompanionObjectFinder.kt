package ru.santaev.companionObjectFormatter

import org.jetbrains.kotlin.psi.*
import java.util.*

class KtCompanionObjectFinder {

    fun findCompanionObjects(file: KtFile): FindResult {
        return CompanionObjectFinderVisitor(file).let { visitor ->
            visitor.visitKtFile(file)
            FindResult(visitor.companionObjects)
        }
    }

    companion object {
        private const val COMPANION_OBJECT_DECLARATION = "companion object"
    }

    data class FindResult(
        val companionObjects: List<CompanionObject>
    )

    data class CompanionObject(
        val ktFile: KtFile,
        val containingClass: KtClass,
        val companionObject: KtObjectDeclaration
    )

    private class CompanionObjectFinderVisitor(private val file: KtFile) : KtTreeVisitorVoid() {

        val companionObjects = mutableListOf<CompanionObject>()
        private var classStack: Deque<KtClass> = ArrayDeque()

        override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
            super.visitObjectDeclaration(declaration)
            if (declaration.text.startsWith(COMPANION_OBJECT_DECLARATION)) {
                val companionBody = declaration.children
                    .filterIsInstance<KtClassBody>()
                    .firstOrNull()
                val containingClass = if (classStack.isEmpty()) null else classStack.peek()
                if (companionBody != null && containingClass != null) {
                    companionObjects.add(CompanionObject(file, containingClass, declaration))
                }
            }
        }

        override fun visitClass(klass: KtClass) {
            if (klass !is KtEnumEntry) {
                classStack.push(klass)
                super.visitClass(klass)
                classStack.pop()
            } else {
                super.visitClass(klass)
            }
        }
    }
}
