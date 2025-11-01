package pl.ejdev.zwoje.core.template.mustache

import pl.ejdev.zwoje.core.exception.TemplateNotFoundException
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class ZwojeMustacheTemplateResolver : ZwojeTemplateResolver<Any>(), TemplateProvider {
    override val type: TemplateType = TemplateType.Mustache
    override val baseDir: String = "src/main/resources"
    override val templatesDir: String = "templates"
    override val extension: String = "mustache"

    private val templates = mutableMapOf<String, ZwojeMustacheTemplate<*>>()

    override fun <T : Any> register(id: String, template: ZwojeTemplate<TemplateInputData<T>, T>) {
        templates[id] = template as ZwojeMustacheTemplate<T>
    }

    override fun get(id: String): ZwojeTemplate<TemplateInputData<Any>, Any> {
        val template = templates[id] ?: throw TemplateNotFoundException(id)
        return template as ZwojeMustacheTemplate<Any>
    }
}
