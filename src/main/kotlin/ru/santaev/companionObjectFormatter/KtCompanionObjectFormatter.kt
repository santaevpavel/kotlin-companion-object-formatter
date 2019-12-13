package ru.santaev.companionObjectFormatter

import mu.KotlinLogging
import org.jetbrains.kotlin.psi.*
import ru.santaev.companionObjectFormatter.KtCompanionObjectFinder.CompanionObject
import ru.santaev.companionObjectFormatter.KtCompanionObjectFinder.FindResult
import ru.santaev.companionObjectFormatter.placementFinder.IKtCompanionObjectPlacementFinder
import ru.santaev.companionObjectFormatter.placementFinder.IKtCompanionObjectPlacementFinder.Placement
import java.io.File

private val logger = KotlinLogging.logger {}

class KtCompanionObjectFormatter(
    private val companionFinder: KtCompanionObjectFinder,
    private val companionMover: KtCompanionObjectMover,
    private val fileParser: KtFileParser,
    private val companionNewPlacementFinder: IKtCompanionObjectPlacementFinder
) {

    fun format(file: File) {
        val ktFile = fileParser.parseFile(file.absolutePath)
        val formatResult = formatKtFile(ktFile)
        when (formatResult) {
            is FormatResult.NoCompanion -> {
                logger.info { "${file.canonicalPath} no need to formatKtFile (no companion object)" }
            }
            is FormatResult.FormatNotNeeded -> {
                logger.info { "${file.canonicalPath} no need to formatKtFile" }
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

    private fun formatKtFile(ktFile: KtFile): FormatResult {
        var formatResult: FormatResult.Formatted? = null
        var currentKtFile: KtFile = ktFile
        while (true) {
            val internalFormatResult = formatKtFileInternal(currentKtFile)
            when (internalFormatResult) {
                is FormatResult.NoCompanion,
                is FormatResult.FormatNotNeeded -> {
                    return formatResult ?: internalFormatResult
                }
                is FormatResult.Formatted -> {
                    formatResult = internalFormatResult
                }
            }

            currentKtFile = fileParser.parseString(formatResult.content)
        }
    }

    private fun formatKtFileInternal(ktFile: KtFile): FormatResult {
        val companionObjectFindResult = companionFinder.findCompanionObjects(ktFile)
        if (companionObjectFindResult.companionObjects.isEmpty()) {
            return FormatResult.NoCompanion
        }

        return moveCompanionObject(ktFile, companionObjectFindResult)
    }

    private fun moveCompanionObject(ktFile: KtFile, companionObjectFindResult: FindResult): FormatResult {
        val companionPlacementToMove = getFirstCompanionObjectsPlacement(ktFile, companionObjectFindResult)
        return if (companionPlacementToMove != null) {
            val content = companionMover.moveCompanionObject(
                ktFile = ktFile,
                companionObject = companionPlacementToMove.first.companionObject,
                moveAfterElement = companionPlacementToMove.second.element
            )
            FormatResult.Formatted(
                content = content,
                affectedClasses = listOf(companionPlacementToMove.first.containingClass)
            )
        } else {
            FormatResult.FormatNotNeeded
        }
    }

    private fun getFirstCompanionObjectsPlacement(
        ktFile: KtFile,
        companionObjectFindResult: FindResult
    ): Pair<CompanionObject, Placement.AfterElement>? {
        return companionObjectFindResult.companionObjects
            .map { companion ->
                val companionNewPlacement = companionNewPlacementFinder.findCompanionObjectNewPlacementLine(
                    ktFile = ktFile,
                    ktClass = companion.containingClass
                )
                companion to companionNewPlacement
            }
            .filter { (_, companionNewPlacement) ->
                companionNewPlacement is Placement.AfterElement
            }
            .map { (companion, companionNewPlacement) ->
                companion to companionNewPlacement as Placement.AfterElement
            }
            .firstOrNull { (companion, companionNewPlacement) ->
                isNeedToMoveCompanionObject(companion.companionObject, companionNewPlacement.element)
            }
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
