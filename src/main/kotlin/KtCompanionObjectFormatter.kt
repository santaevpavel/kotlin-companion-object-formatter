import KtCompanionObjectNewPlacementFinder.Result
import mu.KotlinLogging
import org.jetbrains.kotlin.psi.*
import java.io.File

private val logger = KotlinLogging.logger {}

class KtCompanionObjectFormatter(
    private val companionFinder: KtCompanionObjectFinder,
    private val companionMover: KtCompanionObjectMover,
    private val fileReader: KtFileReader,
    private val companionNewPlacementFinder: KtCompanionObjectNewPlacementFinder
) {

    fun format(file: File) {
        val ktFile = fileReader.read(file.absolutePath)
        val formatResult = format(ktFile)
        when (formatResult) {
            is FormatResult.NoCompanion -> {
                logger.info { "${file.canonicalPath} no need to format (no companion object)" }
            }
            is FormatResult.FormatNotNeeded -> {
                logger.info { "${file.canonicalPath} no need to format" }
            }
            is FormatResult.Formatted -> {
                file.delete()
                file.writeText(formatResult.content)
                logger.info { "${file.canonicalPath} formatted" }
                val affectedClasses = formatResult.affectedClasses.map { it.name }.joinToString()
                logger.info { "Formatted classes: $affectedClasses" }
            }
        }
    }

    private fun format(ktFile: KtFile): FormatResult {
        var formatResult: FormatResult.Formatted? = null
        do {
            val newKtFile = if (formatResult == null) {
                ktFile
            } else {
                fileReader.fromString(formatResult.content)
            }
            val result = formatInternal(newKtFile)
            when (result) {
                is FormatResult.NoCompanion,
                is FormatResult.FormatNotNeeded -> {
                    return formatResult ?: result
                }
                is FormatResult.Formatted -> {
                    formatResult = result
                }
            }
        } while (true)
    }

    private fun formatInternal(ktFile: KtFile): FormatResult {
        val companionObjectFindResult = companionFinder.findCompanionObjects(ktFile)
        val formatResults = companionObjectFindResult.companionObjects.map { companion ->
            val companionNewPlacement = companionNewPlacementFinder.findCompanionObjectNewPlacementLine(
                ktFile = ktFile,
                ktClass = companion.containingClass
            )
            when (companionNewPlacement) {
                is Result.NoPlacement -> {
                    return@map FormatResult.FormatNotNeeded
                }
                is Result.PlacementAfter -> {
                    if (!isNeedToMoveCompanionObject(companion.companionObject, companionNewPlacement.element)) {
                        return@map FormatResult.FormatNotNeeded
                    } else {
                        val content = companionMover.moveCompanionObject(
                            ktFile = ktFile,
                            companionObject = companion.companionObject,
                            moveAfterElement = companionNewPlacement.element
                        )
                        return FormatResult.Formatted(content, listOf(companion.containingClass))
                    }
                }
            }
        }
        return if (formatResults.isEmpty()) {
            FormatResult.NoCompanion
        } else {
            FormatResult.FormatNotNeeded
        }
    }

    private fun isNeedToMoveCompanionObject(
        ktObjectDeclaration: KtObjectDeclaration,
        newPlacementAfter: KtElement
    ): Boolean {
        val classBody = ktObjectDeclaration.parent as? KtClassBody
        val ktElementChildren = classBody?.children?.filter { it is KtElement }
        val companionIdx = ktElementChildren?.indexOf(ktObjectDeclaration)
        if (companionIdx == null || companionIdx < 0) {
            return false
        }
        val currentObjectTopElement = classBody.children.getOrNull(companionIdx - 1)
        return newPlacementAfter != currentObjectTopElement
    }

    private sealed class FormatResult {

        object NoCompanion : FormatResult()

        object FormatNotNeeded : FormatResult()

        class Formatted(
            val content: String,
            val affectedClasses: List<KtClass>
        ) : FormatResult()
    }
}