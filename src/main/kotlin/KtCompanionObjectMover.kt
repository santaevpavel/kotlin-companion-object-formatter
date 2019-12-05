import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration

class KtCompanionObjectMover(
    private val elementBoundFinder: KtElementBoundFinder
) {

    fun moveCompanionObject(
        ktFile: KtFile,
        companionObject: KtObjectDeclaration,
        moveAfterElement: KtElement
    ): String {
        val content = ktFile.text
        val companionBounds = elementBoundFinder.find(ktFile, companionObject)
        val topElementBounds = elementBoundFinder.find(ktFile, moveAfterElement)
        val contentLines = content.lines().toMutableList()
        val companionObjectLines = content.lines().filterIndexed { index, _ -> (index + 1) in companionBounds.range }
        val numberOfRemovedLines = removeCompanionObject(contentLines, companionBounds)
        val companionNewPlacementLine = topElementBounds.endLine - numberOfRemovedLines
        insertCompanionObject(companionObjectLines, contentLines, companionNewPlacementLine)
        return contentLines.joinToString(LINE_SEPARATOR)
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

