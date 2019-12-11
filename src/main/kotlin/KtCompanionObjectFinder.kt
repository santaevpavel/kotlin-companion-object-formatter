import org.jetbrains.kotlin.psi.*


class KtCompanionObjectFinder {

    fun findCompanionObjects(file: KtFile): FindResult {
        val companionObjects = mutableListOf<CompanionObject>()

        var currentClass: KtClass? = null
        object : KtTreeVisitorVoid() {

            override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
                super.visitObjectDeclaration(declaration)
                if (declaration.text.startsWith(COMPANION_OBJECT_DECLARATION)) {
                    val companionBody = declaration.children
                        .filterIsInstance<KtClassBody>()
                        .firstOrNull()
                    val containingClass = currentClass
                    if (companionBody != null && containingClass != null) {
                        companionObjects.add(CompanionObject(file, containingClass, declaration))
                    }
                }
            }

            override fun visitClass(klass: KtClass) {
                if (klass !is KtEnumEntry) {
                    currentClass = klass
                    super.visitClass(klass)
                    currentClass = null
                } else {
                    super.visitClass(klass)
                }
            }
        }.visitKtFile(file)
        return FindResult(companionObjects)
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
}

