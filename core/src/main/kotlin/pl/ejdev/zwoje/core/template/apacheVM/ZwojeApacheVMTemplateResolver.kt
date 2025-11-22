package pl.ejdev.zwoje.core.template.apacheVM

import pl.ejdev.zwoje.core.exception.TemplateNotFoundException
import pl.ejdev.zwoje.core.template.*
import kotlin.collections.set

class ZwojeApacheVMTemplateResolver() : ZwojeTemplateResolver<Any>(), TemplateProvider {
    override val type: TemplateType = TemplateType.ApacheVM
    override val baseDir: String = "src/main/resources"
    override val templatesDir: String = "templates"
    override val extension: String = "vm"

    private val templates = mutableMapOf<String, ZwojeApacheVMTemplate<*>>()

    override fun <T : Any> register(id: String, template: ZwojeTemplate<TemplateInputData<T>, T>) {
        templates[id] = template as ZwojeApacheVMTemplate<T>

    }

    @Suppress("UNCHECKED_CAST")
    override fun get(id: String): ZwojeTemplate<TemplateInputData<Any>, Any> {
        val template = templates[id] ?: throw TemplateNotFoundException(id)
        return template as ZwojeApacheVMTemplate<Any>
    }

    override fun exists(id: String): Boolean = templates[id] != null

}
