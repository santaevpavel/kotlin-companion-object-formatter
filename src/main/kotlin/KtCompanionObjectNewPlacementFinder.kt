import org.jetbrains.kotlin.psi.*

class KtCompanionObjectNewPlacementFinder {

    fun findCompanionObjectNewPlacementLine(
        ktFile: KtFile,
        ktClass: KtClass
    ): Result {
        val classBody = ktClass.children
            .filterIsInstance<KtClassBody>()
            .firstOrNull()
            ?: return Result.NoPlacement
        val lastNonClassElement = getFunctionsAndClasses(classBody)
            .lastOrNull { ktElement ->
                ktElement !is KtClass && ktElement !is KtObjectDeclaration
            }
        return if (lastNonClassElement == null) {
            Result.NoPlacement
        } else {
            Result.PlacementAfter(lastNonClassElement)
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

            override fun visitProperty(property: KtProperty) {
                super.visitProperty(property)
                functionsAndClasses.add(property)
            }
        }
        classBody.acceptChildren(visitor)
        return functionsAndClasses
    }

    sealed class Result {

        object NoPlacement : Result()

        class PlacementAfter(val element: KtElement) : Result()
    }
}