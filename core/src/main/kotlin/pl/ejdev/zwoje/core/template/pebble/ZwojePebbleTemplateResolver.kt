package pl.ejdev.zwoje.core.template.pebble

import pl.ejdev.zwoje.core.exception.TemplateNotFoundException
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class ZwojePebbleTemplateResolver : ZwojeTemplateResolver<Any>(), TemplateProvider {
    override val type: TemplateType = TemplateType.Pebble
    override val templatePath: String = "src/main/resources/templates/"
    override val extension: String = "peb"

    private val templates = mutableMapOf<String, ZwojePebbleTemplate<*>>()

    override fun <T : Any> register(id: String, template: ZwojeTemplate<TemplateInputData<T>, T>) {
        templates[id] = template as ZwojePebbleTemplate<T>
    }

    override fun get(id: String): ZwojeTemplate<TemplateInputData<Any>, Any> {
        val template = templates[id] ?: throw TemplateNotFoundException(id)
        return template as ZwojePebbleTemplate<Any>
    }
}
