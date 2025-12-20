package pl.ejdev.zwoje.core.template.jasper

import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class JasperTemplateResolver(
    override val type: TemplateType = TemplateType.Jasper,
    override val baseDir: String = "src/main/resources",
    override val templatesDir: String = "templates",
    override val extension: String = "jrxml",
) : ZwojeTemplateResolver<Any>(), TemplateProvider {

    private val registry = mutableMapOf<String, ZwojeTemplate<TemplateInputData<Any>, Any>>()

    override fun <T : Any> register(id: String, template: ZwojeTemplate<TemplateInputData<T>, T>) {
        @Suppress("UNCHECKED_CAST")
        registry[id] = template as ZwojeTemplate<TemplateInputData<Any>, Any>
    }

    override fun get(id: String): ZwojeTemplate<TemplateInputData<Any>, Any> =
        registry[id] ?: error("Template not found: $id")

    override fun exists(id: String): Boolean = registry.containsKey(id)
}
