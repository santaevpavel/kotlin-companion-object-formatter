import org.jetbrains.kotlin.psi.KtFile
import java.io.File

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
            println(result)
            file.delete()
            file.writeText(result)
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