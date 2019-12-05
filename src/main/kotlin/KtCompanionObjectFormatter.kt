import mu.KotlinLogging
import org.jetbrains.kotlin.psi.KtFile
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
        val result = format(ktFile)
        if (result != null) {
            logger.info { "${file.absolutePath} formatted" }
            file.delete()
            file.writeText(result)
        } else {
            logger.info { "${file.absolutePath} no need to format" }
        }
    }

    private fun format(ktFile: KtFile): String? {
        val companionObjectFindResult = companionFinder.findCompanionObjects(ktFile)
        val companion = companionObjectFindResult.companionObjects.firstOrNull() ?: return null
        val companionNewPlacement = companionNewPlacementFinder.findCompanionObjectNewPlacementLine(
            ktFile = ktFile,
            ktClass = companion.containingClass
        )
        return when (companionNewPlacement) {
            is KtCompanionObjectNewPlacementFinder.Result.NoPlacement -> {
                null
            }
            is KtCompanionObjectNewPlacementFinder.Result.PlacementAfter -> {
                companionMover.moveCompanionObject(
                    ktFile = ktFile,
                    companionObject = companion.companionObject,
                    moveAfterElement = companionNewPlacement.element
                )
            }
        }
    }
}