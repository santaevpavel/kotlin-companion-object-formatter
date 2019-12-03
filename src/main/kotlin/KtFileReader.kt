import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtilRt
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

class KtFileReader {

    private val psiFileFactory: PsiFileFactory =
        PsiFileFactory.getInstance(createKotlinCoreEnvironment().project)

    fun read(path: String): KtFile {
        val content = File(path).readText()
        return psiFileFactory.createFileFromText(
            path,
            KotlinLanguage.INSTANCE,
            StringUtilRt.convertLineSeparators(content)
        ) as KtFile
    }

    fun fromString(content: String): KtFile {
        return psiFileFactory.createFileFromText(
            "",
            KotlinLanguage.INSTANCE,
            StringUtilRt.convertLineSeparators(content)
        ) as KtFile
    }

    private fun createKotlinCoreEnvironment(configuration: CompilerConfiguration = CompilerConfiguration()): KotlinCoreEnvironment {
        // https://github.com/JetBrains/kotlin/commit/2568804eaa2c8f6b10b735777218c81af62919c1
        setIdeaIoUseFallback()
        configuration.put(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false)
        )
        val environment = KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(),
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
        return environment
    }
}