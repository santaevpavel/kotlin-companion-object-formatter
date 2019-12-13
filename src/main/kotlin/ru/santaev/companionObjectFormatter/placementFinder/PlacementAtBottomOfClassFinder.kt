package ru.santaev.companionObjectFormatter.placementFinder

import org.jetbrains.kotlin.psi.*

class PlacementAtBottomOfClassFinder : IKtCompanionObjectPlacementFinder {

    override fun findCompanionObjectNewPlacementLine(
        ktFile: KtFile,
        ktClass: KtClass
    ): IKtCompanionObjectPlacementFinder.Placement {
        val classBody = ktClass.children
            .filterIsInstance<KtClassBody>()
            .firstOrNull()
            ?: return IKtCompanionObjectPlacementFinder.Placement.None
        val lastNonClassElement = getFunctionsAndClasses(classBody)
            .lastOrNull { ktElement ->
                ktElement !is KtClass && ktElement !is KtObjectDeclaration
            }
        return if (lastNonClassElement == null) {
            IKtCompanionObjectPlacementFinder.Placement.None
        } else {
            IKtCompanionObjectPlacementFinder.Placement.AfterElement(
                lastNonClassElement
            )
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
}
