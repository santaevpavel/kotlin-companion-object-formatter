package ru.santaev.companionObjectFormatter.utils

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class KtFunctionFinder(private val name: String) : KtTreeVisitorVoid() {

    var function: KtNamedFunction? = null

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (function.name == name) {
            this.function = function
        }
    }
}

class KtClassFinder(private val name: String) : KtTreeVisitorVoid() {

    var klass: KtClass? = null

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (klass.name == name) {
            this.klass = klass
        }
    }
}
