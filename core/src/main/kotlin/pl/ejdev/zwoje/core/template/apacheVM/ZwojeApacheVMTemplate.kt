package pl.ejdev.zwoje.core.template.apacheVM;

import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import java.io.StringWriter
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler


abstract class ZwojeApacheVMTemplate<INPUT : Any>(
val templateName: String
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {
    override val templatePath: String? = null

    override fun compile(input: TemplateInputData<INPUT>): String {
        val template = cfg.getTemplate("$templateName.vm")
        val writer = StringWriter()
        template.process(input.data, writer)
        return writer.toString()
    }

    private companion object {
        private val cfg: Configuration by lazy {
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
