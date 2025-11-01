package pl.ejdev.zwoje.core.template.groovyTemplates

import pl.ejdev.zwoje.core.exception.TemplateNotFoundException
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class ZwojeGroovyMarkupTemplateResolver() : ZwojeTemplateResolver<Any>(), TemplateProvider {
    override val type: TemplateType = TemplateType.GroovyTemplate
    override val baseDir: String = "src/main/resources"
    override val templatesDir: String = "templates"
    override val extension: String = "gtmpl"

    private val templates = mutableMapOf<String, ZwojeGroovyMarkupTemplate<*>>()

    override fun <T : Any> register(id: String, template: ZwojeTemplate<TemplateInputData<T>, T>) {
        templates[id] = template as ZwojeGroovyMarkupTemplate<T>
    }

    override fun get(id: String): ZwojeTemplate<TemplateInputData<Any>, Any> {
        val template = templates[id] ?: throw TemplateNotFoundException(id)
        return template as ZwojeGroovyMarkupTemplate<Any>
    }
}

