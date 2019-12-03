import org.jetbrains.kotlin.psi.*

class KtCompanionFormatter {

    fun format(
        ktFile: KtFile,
        containingClass: KtClass,
        companionObject: KtObjectDeclaration
    ) {
        /*val companionObjectStartLine = companionObject.node.line(ktFile)
        val companionObjectEndLine = companionObject.node.line(ktFile)*/
    }

}

class KtCompanionObjectNewPlacementFinder {

    fun findCompanionObjectNewPlacementLine(
        ktFile: KtFile,
        ktClass: KtClass
    ): Results {
        val classBody = ktClass.children
            .filterIsInstance<KtClassBody>()
            .firstOrNull()
            ?: return Results.NoPlacement
        val lastNonClassElement = getFunctionsAndClasses(classBody).lastOrNull { it !is KtClass }
        return if (lastNonClassElement == null) {
            Results.NoPlacement
        } else {
            Results.PlacementAfter(lastNonClassElement)
        }
    }

    private fun getFunctionsAndClasses(classBody: KtClassBody): MutableList<KtElement> {
        val functionsAndClasses = mutableListOf<KtElement>()
        val visitor = object : KtVisitorVoid() {

            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                functionsAndClasses.add(function)
            }

            override fun visitClass(klass: KtClass) {
                super.visitClass(klass)
                functionsAndClasses.add(klass)
            }
        }
        classBody.acceptChildren(visitor)
        return functionsAndClasses
    }

    sealed class Results {

        object NoPlacement : Results()

        class PlacementAfter(val element: KtElement) : Results()
    }
}