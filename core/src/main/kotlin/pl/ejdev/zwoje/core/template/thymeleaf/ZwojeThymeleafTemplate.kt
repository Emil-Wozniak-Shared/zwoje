package pl.ejdev.zwoje.core.template.thymeleaf

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.utils.getMembers

abstract class ZwojeThymeleafTemplate<INPUT : Any>(
    private val templateName: String
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {
    override fun compile(input: TemplateInputData<INPUT>): String =
        templateEngine.process(templateName, input.toContext())

    private fun TemplateInputData<INPUT>.toContext(): Context = Context().apply {
        getMembers<INPUT>().forEach { (name, value) -> setVariable(name, value) }
    }

    companion object {
        private val templateEngine: TemplateEngine by lazy {
            val resolver = ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
                suffix = ".html"
                characterEncoding = "UTF-8"
                templateMode = TemplateMode.HTML
            }
            TemplateEngine().apply { setTemplateResolver(resolver) }
        }
    }
}
