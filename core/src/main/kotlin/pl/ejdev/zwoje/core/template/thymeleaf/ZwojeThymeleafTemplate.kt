package pl.ejdev.zwoje.core.template.thymeleaf

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.utils.getMembers
import java.io.File

abstract class ZwojeThymeleafTemplate<INPUT : Any>(
    private val templateName: String,
    override val templatePath: String?
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {
    val engine: TemplateEngine = if (templatePath == null) classpathEngine else templateEngine(templatePath!!)

    override fun compile(input: TemplateInputData<INPUT>): String =
        engine.process(templateName, input.toContext())

    private fun TemplateInputData<INPUT>.toContext(): Context = Context().apply {
        getMembers().forEach { (name, value) -> setVariable(name, value) }
    }

    private companion object {
        private val classpathEngine: TemplateEngine by lazy {
            val resolver = ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
                suffix = ".html"
                characterEncoding = "UTF-8"
                templateMode = TemplateMode.HTML
            }
            TemplateEngine().apply { setTemplateResolver(resolver) }
        }

        private fun templateEngine(templatePath: String): TemplateEngine {
            val directory = File(templatePath).parentFile?.absolutePath ?: "./"
            val resolver = FileTemplateResolver().apply {
                prefix = "$directory${File.separator}"
                suffix = "" // disable suffix — we’ll pass full name
                characterEncoding = "UTF-8"
                templateMode = TemplateMode.HTML
                isCacheable = false // so we can refresh instantly in IJ preview
            }
            return TemplateEngine().apply { setTemplateResolver(resolver) }
        }
    }
}
