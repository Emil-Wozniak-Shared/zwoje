package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreeMarkerTemplate
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreeMarkerTemplateResolver
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplate
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplateResolver
import pl.ejdev.zwoje.core.template.kotlinx.ZwojeKotlinHtmlTemplateResolver
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplate
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplateResolver
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplate
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplateResolver
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplate
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplateResolver

@Service(Service.Level.PROJECT)
class OpenHtmlEngineCompileService {
    private val compileEngine = OpenHtmlToPdfCompileEngine()

    fun compile(
        resolver: ZwojeTemplateResolver<Any>,
        id: String,
        input: IJTemplateInputData,
        templatePath: String
    ): ByteArray {
        val engine = ZwojeEngine(compileEngine, resolver)
        val template = template(resolver, id, templatePath)
        if (!resolver.exists(id)) {
            resolver.register(id, template)
        }
        val bytes = engine.compile(id, input)
        return bytes
    }

    private fun template(resolver: ZwojeTemplateResolver<Any>, id: String, templatePath: String) = when (resolver) {
        is ZwojeThymeleafTemplateResolver -> IJZwojeThymeleafTemplate(id, templatePath)
        is ZwojeGroovyMarkupTemplateResolver -> IJZwojeGroovyMarkupTemplate(id)
        is ZwojeMustacheTemplateResolver -> IJZwojeMustacheTemplate(id)
        is ZwojeFreeMarkerTemplateResolver -> IJZwojeFreeMarkerTemplate(id)
        is ZwojePebbleTemplateResolver -> IJZwojePebbleTemplate(id)
        is ZwojeKotlinHtmlTemplateResolver -> TODO("kotlinx html is not ready")
        else -> error("Unsupported ${resolver::class.java.simpleName}")
    }

    class IJZwojeThymeleafTemplate(name: String, templatePath: String) :
        ZwojeThymeleafTemplate<IJTemplateInputData>(name, templatePath)

    class IJZwojeGroovyMarkupTemplate(name: String) : ZwojeGroovyMarkupTemplate<IJTemplateInputData>(name)
    class IJZwojeMustacheTemplate(name: String) : ZwojeMustacheTemplate<IJTemplateInputData>(name)
    class IJZwojeFreeMarkerTemplate(name: String) : ZwojeFreeMarkerTemplate<IJTemplateInputData>(name)
    class IJZwojePebbleTemplate(name: String) : ZwojePebbleTemplate<IJTemplateInputData>(name)

    class IJTemplateInputData(input: Any) : TemplateInputData<Any>(input)
}