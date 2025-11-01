package pl.ejdev.zwoje.core.template.freemarker

import pl.ejdev.zwoje.core.exception.TemplateNotFoundException
import pl.ejdev.zwoje.core.template.*

class ZwojeFreeMarkerTemplateResolver() : ZwojeTemplateResolver<Any>(), TemplateProvider {
    override val type: TemplateType = TemplateType.FreeMarker
    override val baseDir: String = "src/main/resources"
    override val templatesDir: String = "templates"
    override val extension: String = "ftl"

    private val templates = mutableMapOf<String, ZwojeFreeMarkerTemplate<*>>()

    override fun <T : Any> register(id: String, template: ZwojeTemplate<TemplateInputData<T>, T>) {
        templates[id] = template as ZwojeFreeMarkerTemplate<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(id: String): ZwojeTemplate<TemplateInputData<Any>, Any> {
        val template = templates[id] ?: throw TemplateNotFoundException(id)
        return template as ZwojeFreeMarkerTemplate<Any>
    }
}
