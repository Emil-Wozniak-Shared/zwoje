package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine
import pl.ejdev.zwoje.core.template.*
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreeMarkerTemplate
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplate
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplate
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplate
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplate
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplateResolver

@Service(Service.Level.PROJECT)
class OpenHtmlEngineCompileService {
    private val compileEngine = OpenHtmlToPdfCompileEngine()

    fun compile(resolver: ZwojeTemplateResolver<Any>, id: String, templatePath: String, content: String): ByteArray {
        val templateType = resolver.type
        val engine = ZwojeEngine(compileEngine, resolver)
        val template = template(templateType, id, templatePath)
        if (!resolver.exists(id)) {
            resolver.register(id, template)
        }
        val parser = parser(templateType, id, resolver)
        val parsed = parser.parse(content)
        val input = IJTemplateInputData(parsed)
        val bytes = engine.compile(id, input)
        return bytes
    }

    private fun parser(
        templateType: TemplateType,
        id: String,
        resolver: ZwojeTemplateResolver<Any>
    ): ZwojeTemplateParser<Any> = when (templateType) {
        TemplateType.Thymeleaf -> (resolver as ZwojeThymeleafTemplateResolver).getParser(id)
        else -> stubTemplateParser
    }

    private fun template(type: TemplateType, id: String, templatePath: String) = when (type) {
        TemplateType.Thymeleaf -> IJZwojeThymeleafTemplate(id, templatePath)
        TemplateType.GroovyTemplate -> IJZwojeGroovyMarkupTemplate(id)
        TemplateType.Mustache -> IJZwojeMustacheTemplate(id)
        TemplateType.FreeMarker -> IJZwojeFreeMarkerTemplate(id)
        TemplateType.Pebble -> IJZwojePebbleTemplate(id)
        TemplateType.KotlinxHtml -> TODO("kotlinx html is not ready")
    }

    class IJZwojeThymeleafTemplate(name: String, templatePath: String) :
        ZwojeThymeleafTemplate<IJTemplateInputData>(name, templatePath)

    class IJZwojeGroovyMarkupTemplate(name: String) : ZwojeGroovyMarkupTemplate<IJTemplateInputData>(name)
    class IJZwojeMustacheTemplate(name: String) : ZwojeMustacheTemplate<IJTemplateInputData>(name)
    class IJZwojeFreeMarkerTemplate(name: String) : ZwojeFreeMarkerTemplate<IJTemplateInputData>(name)
    class IJZwojePebbleTemplate(name: String) : ZwojePebbleTemplate<IJTemplateInputData>(name)

    class IJTemplateInputData(input: Any) : TemplateInputData<Any>(input)

    private companion object {
        val stubTemplateParser by lazy {
            object : ZwojeTemplateParser<Any> {
                override fun parse(content: String): Set<TemplateVariable> = setOf()

            }
        }
    }
}