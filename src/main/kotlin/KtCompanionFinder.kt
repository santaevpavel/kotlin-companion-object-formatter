import org.jetbrains.kotlin.psi.*


fun main() {

    val ktFile =
        KtFileReader().read("C:\\Users\\workLaptop\\Documents\\Projects\\companion-formatter\\src\\main\\kotlin\\Sample.kt")

    KtCompanionFinder().findCompanionObjects(ktFile)
}

class KtCompanionFinder {

    companion object {
        private const val COMPANION_OBJECT_DECLARATION = "companion object"
    }

    fun findCompanionObjects(file: KtFile): FindResult {
        val companionObjects = mutableListOf<CompanionObject>()

        var currentClass: KtClass? = null
        object : KtTreeVisitorVoid() {

            override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
                super.visitObjectDeclaration(declaration)
                declaration.text.startsWith(COMPANION_OBJECT_DECLARATION)
                val companionBody = declaration.children
                    .filterIsInstance(KtClassBody::class.java)
                    .firstOrNull()
                val containingClass = currentClass
                if (companionBody != null && containingClass != null) {
                    companionObjects.add(CompanionObject(file, containingClass, declaration))
                }
            }

            override fun visitClass(klass: KtClass) {
                currentClass = klass
                super.visitClass(klass)
                currentClass = null
            }
        }.visitKtFile(file)
        return FindResult(companionObjects)
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

