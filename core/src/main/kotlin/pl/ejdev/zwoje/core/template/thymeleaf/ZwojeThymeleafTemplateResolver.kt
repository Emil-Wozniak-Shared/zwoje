package pl.ejdev.zwoje.core.template.thymeleaf

import org.thymeleaf.Thymeleaf
import pl.ejdev.zwoje.core.exception.TemplateNotFoundException
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class ZwojeThymeleafTemplateResolver() : ZwojeTemplateResolver<Any>(), TemplateProvider {
    override val type: TemplateType = TemplateType.Thymeleaf
    override val baseDir: String = "src/main/resources"
    override val templatesDir: String = "templates"
    override val extension: String = "html"

    private val templates = mutableMapOf<String, ZwojeThymeleafTemplate<*>>()

    override fun <T : Any> register(id: String, template: ZwojeTemplate<TemplateInputData<T>, T>) {
        templates[id] = template as ZwojeThymeleafTemplate<T>
        Thymeleaf.getVersionMajor()
    }

    override fun get(id: String): ZwojeTemplate<TemplateInputData<Any>, Any> {
        val template = templates[id] ?: throw TemplateNotFoundException(id)
        return template as ZwojeThymeleafTemplate<Any>
    }

    override fun exists(id: String): Boolean = templates[id] != null
}
