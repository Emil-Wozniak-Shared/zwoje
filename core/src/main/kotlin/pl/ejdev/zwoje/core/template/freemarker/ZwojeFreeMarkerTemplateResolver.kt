package pl.ejdev.zwoje.core.template.freemarker

import pl.ejdev.zwoje.core.exception.TemplateNotFoundException
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class ZwojeFreeMarkerTemplateResolver : ZwojeTemplateResolver<Any>() {
    private val templates = mutableMapOf<String, ZwojeFreeMarkerTemplate<*>>()

    override fun <T : Any> register(id: String, template: ZwojeTemplate<TemplateInputData<T>, T>) {
        templates[id] = template as ZwojeFreeMarkerTemplate<T>
    }

    override fun get(id: String): ZwojeTemplate<TemplateInputData<Any>, Any> {
        val template = templates[id] ?: throw TemplateNotFoundException(id)
        return template as ZwojeFreeMarkerTemplate<Any>
    }
}
