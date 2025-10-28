package pl.ejdev.zwoje.core.template.kotlinx

import pl.ejdev.zwoje.core.exception.TemplateNotFoundException
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.kotlinx.ZwojeKotlinHtmlTemplate
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class ZwojeKotlinHtmlTemplateResolver : ZwojeTemplateResolver<Any>() {
    override val type: TemplateType = TemplateType.KotlinxHtml

    private val templates = mutableMapOf<String, ZwojeKotlinHtmlTemplate<*>>()

    override fun <T : Any> register(id: String, template: ZwojeTemplate<TemplateInputData<T>, T>) {
        templates[id] = template as ZwojeKotlinHtmlTemplate<T>
    }

    override fun get(id: String): ZwojeTemplate<TemplateInputData<Any>, Any> {
        val template = templates[id] ?: throw TemplateNotFoundException(id)
        return template as ZwojeKotlinHtmlTemplate<Any>
    }
}