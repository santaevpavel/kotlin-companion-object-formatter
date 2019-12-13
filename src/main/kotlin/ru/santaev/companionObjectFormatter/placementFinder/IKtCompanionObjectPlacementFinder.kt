package ru.santaev.companionObjectFormatter.placementFinder

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile

interface IKtCompanionObjectPlacementFinder {

    fun findCompanionObjectNewPlacementLine(ktFile: KtFile, ktClass: KtClass): Placement

    sealed class Placement {

        object None : Placement()

        class AfterElement(val element: KtElement) : Placement()
    }
}
