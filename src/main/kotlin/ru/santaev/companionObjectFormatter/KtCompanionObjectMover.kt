package ru.santaev.companionObjectFormatter

import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration

class KtCompanionObjectMover(
    private val elementBoundFinder: KtElementBoundFinder
) {

    fun moveCompanionObject(
        ktFile: KtFile,
        companionObject: KtObjectDeclaration,
        tallElement: KtElement
    ): String {
        val content = ktFile.text
        val companionBounds = elementBoundFinder.find(ktFile, companionObject)
        val contentLines = content.lines().toMutableList()
        val companionObjectLines = content.lines().filterIndexed { index, _ -> (index + 1) in companionBounds.range }
        val numberOfRemovedLines = removeCompanionObject(contentLines, companionBounds)
        val companionInsertionLine = getCompanionInsertionLine(
            ktFile = ktFile,
            tallElement = tallElement,
            numberOfRemovedLines = numberOfRemovedLines,
            previousCompanionPlacementLine = companionBounds.startLine
        )
        insertCompanionObject(companionObjectLines, contentLines, companionInsertionLine)
        return contentLines.joinToString(LINE_SEPARATOR)
    }

    private fun getCompanionInsertionLine(
        ktFile: KtFile,
        tallElement: KtElement,
        numberOfRemovedLines: Int,
        previousCompanionPlacementLine: Int
    ): Int {
        val tallElementBounds = elementBoundFinder.find(ktFile, tallElement)
        val isNeedToMoveUpCompanion = tallElementBounds.endLine < previousCompanionPlacementLine
        return if (isNeedToMoveUpCompanion) {
            tallElementBounds.endLine
        } else {
            tallElementBounds.endLine - numberOfRemovedLines
        }
    }

    private fun removeCompanionObject(
        contentLines: MutableList<String>,
        companionBounds: KtElementBoundFinder.Bounds
    ): Int {
        val companionHeight = companionBounds.endLine - companionBounds.startLine + 1
        val hasCompanionBlankTopLine = contentLines.getOrNull(companionBounds.startLine - 2)?.isBlank() == true
        val hasCompanionBlankBottomLine = contentLines.getOrNull(companionBounds.endLine)?.isBlank() == true
        val isNeedToRemoveBlankLine = hasCompanionBlankTopLine && hasCompanionBlankBottomLine
        val numberOfLinesToRemove = if (isNeedToRemoveBlankLine) {
            companionHeight + 1
        } else {
            companionHeight
        }
        repeat(numberOfLinesToRemove) {
            contentLines.removeAt(companionBounds.startLine - 1)
        }
        return numberOfLinesToRemove
    }

    private fun insertCompanionObject(
        companionObjectLines: List<String>,
        contentLines: MutableList<String>,
        companionNewPlacementLine: Int
    ) {
        contentLines.add(companionNewPlacementLine, "")
        companionObjectLines
            .forEachIndexed { index, line ->
                contentLines.add(companionNewPlacementLine + index + 1, line)
            }
    }

    companion object {
        private const val LINE_SEPARATOR = "\n"
    }
}
