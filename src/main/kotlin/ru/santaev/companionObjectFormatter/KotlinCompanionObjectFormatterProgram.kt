package ru.santaev.companionObjectFormatter

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import ru.santaev.companionObjectFormatter.placementFinder.PlacementAtBottomOfClassFinder
import java.io.File

fun main(args: Array<String>) {
    KotlinCompanionObjectFormatterProgram().main(args)
}

class KotlinCompanionObjectFormatterProgram : CliktCommand() {

    private val path: File by argument(help = "Path of directory with kotlin files").file()

    override fun run() {
        format(path)
    }

    private fun format(file: File) {
        if (file.isDirectory) {
            formatFilesInDirectory(file)
        } else {
            formatFile(file)
        }
    }

    private fun formatFilesInDirectory(file: File) {
        file.listFiles { child ->
            if (child.isDirectory) {
                true
            } else {
                child.name.endsWith(KT_FILE_EXTENSION)
            }
        }
            ?.forEach { fileInDirectory -> format(fileInDirectory) }
    }

    private fun formatFile(file: File) {
        KtCompanionObjectFormatter(
            companionFinder = KtCompanionObjectFinder(),
            companionMover = KtCompanionObjectMover(KtElementBoundFinder()),
            fileParser = KtFileParser(),
            companionNewPlacementFinder = PlacementAtBottomOfClassFinder()
        ).format(file)
    }

    companion object {
        private const val KT_FILE_EXTENSION = ".kt"
    }
}
