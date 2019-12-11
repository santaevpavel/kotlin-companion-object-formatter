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
        var currentKtFile: KtFile = ktFile
        while (true) {
            val internalFormatResult = formatInternal(currentKtFile)
            when (internalFormatResult) {
                is FormatResult.NoCompanion,
                is FormatResult.FormatNotNeeded -> {
                    return formatResult ?: internalFormatResult
                }
                is FormatResult.Formatted -> {
                    formatResult = internalFormatResult
                }
            }

            currentKtFile = fileReader.fromString(formatResult.content)
        }
    }

    private fun formatInternal(ktFile: KtFile): FormatResult {
        val companionObjectFindResult = companionFinder.findCompanionObjects(ktFile)
        if (companionObjectFindResult.companionObjects.isEmpty()) {
            return FormatResult.NoCompanion
        }

        val companionPlacementToMove = companionObjectFindResult.companionObjects
            .map { companion ->
                val companionNewPlacement = companionNewPlacementFinder.findCompanionObjectNewPlacementLine(
                    ktFile = ktFile,
                    ktClass = companion.containingClass
                )
                companion to companionNewPlacement
            }
            .filter { (_, companionNewPlacement) -> companionNewPlacement is Result.PlacementAfter }
            .map { (a, b) -> a to b as Result.PlacementAfter }
            .firstOrNull { (companion, companionNewPlacement) ->
                isNeedToMoveCompanionObject(companion.companionObject, companionNewPlacement.element)
            }

        if (companionPlacementToMove != null) {
            val content = companionMover.moveCompanionObject(
                ktFile = ktFile,
                companionObject = companionPlacementToMove.first.companionObject,
                moveAfterElement = companionPlacementToMove.second.element
            )
            return FormatResult.Formatted(content, listOf(companionPlacementToMove.first.containingClass))
        }

        return FormatResult.FormatNotNeeded
    }

    private fun isNeedToMoveCompanionObject(
        ktObjectDeclaration: KtObjectDeclaration,
        newPlacementAfter: KtElement
    ): Boolean {
        val classBody = ktObjectDeclaration.parent as? KtClassBody
        val ktElementChildren = classBody?.children?.filterIsInstance<KtElement>()
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