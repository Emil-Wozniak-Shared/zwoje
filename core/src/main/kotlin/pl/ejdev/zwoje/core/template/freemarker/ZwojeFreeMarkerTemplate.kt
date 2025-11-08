package pl.ejdev.zwoje.core.template.freemarker

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import java.io.File
import java.io.StringWriter
abstract class ZwojeFreeMarkerTemplate<INPUT : Any>(
    val templateName: String,
    override val templatePath: String? = null
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {

    override fun compile(input: TemplateInputData<INPUT>): String {
        val effectivePath = templatePath ?: "$templateName.ftl"
        val config = getConfigurationForPath(effectivePath)
        val template = config.getTemplate(getTemplateName(effectivePath))
        val writer = StringWriter()
        template.process(input.data, writer)
        return writer.toString()
    }

    private fun getConfigurationForPath(path: String): Configuration {
        return if (isFullPath(path)) {
            getFileSystemConfiguration(path)
        } else {
            classpathConfiguration
        }
    }

    private fun isFullPath(path: String): Boolean {
        // Check for absolute paths (Unix/Windows) or explicit file system indicators
        return path.startsWith("/") ||
                path.startsWith("\\") ||
                path.matches(Regex("^[A-Za-z]:.*")) || // Windows drive letter
                File(path).isAbsolute
    }

    private fun getTemplateName(path: String): String {
        return if (isFullPath(path)) {
            // For full paths, extract just the filename
            File(path).name
        } else {
            // For classpath resources, use as-is
            path
        }
    }

    private fun getFileSystemConfiguration(fullPath: String): Configuration {
        val file = File(fullPath)
        val directory = file.parentFile?.absolutePath ?: "."

        return Configuration(Configuration.VERSION_2_3_33).apply {
            setDirectoryForTemplateLoading(File(directory))
            defaultEncoding = "UTF-8"
            templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
            logTemplateExceptions = false
            wrapUncheckedExceptions = true
            fallbackOnNullLoopVariable = false
        }
    }

    private companion object {
        private val classpathConfiguration: Configuration by lazy {
            Configuration(Configuration.VERSION_2_3_33).apply {
                setClassLoaderForTemplateLoading(
                    Thread.currentThread().contextClassLoader,
                    "templates"
                )
                defaultEncoding = "UTF-8"
                templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
                logTemplateExceptions = false
                wrapUncheckedExceptions = true
                fallbackOnNullLoopVariable = false
            }
        }
    }
}