package pl.ejdev.zwoje.core.template.thymeleaf

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.utils.dataClassMembersFilter

abstract class ZwojeThymeleafTemplate<INPUT : Any>(
    private val templateName: String
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {

    companion object {
        private val templateEngine: TemplateEngine by lazy {
            val resolver = ClassLoaderTemplateResolver().apply {
                prefix = "templates/"         // classpath folder
                suffix = ".html"              // or ".thymeleaf" if you prefer
                characterEncoding = "UTF-8"
                templateMode = TemplateMode.HTML
            }
            TemplateEngine().apply { setTemplateResolver(resolver) }
        }
    }

    override fun compile(input: TemplateInputData<INPUT>): String {
        val context = Context().apply {
            // inject all fields of input.data into the template context
            val data = input.data
            data::class.members
                .filter(dataClassMembersFilter)
                .mapNotNull { member ->
                    member.runCatching { name to this.call(data) }.getOrNull()
                }
                .forEach { (name, value) ->
                    setVariable(name, value)
                }
        }

        return templateEngine.process(templateName, context)
    }
}
