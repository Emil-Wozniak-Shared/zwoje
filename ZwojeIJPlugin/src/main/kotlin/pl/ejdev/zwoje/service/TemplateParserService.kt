package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreemarkerTemplateParser
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplateParser
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplateParser
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplateParser
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplateParser

@Service(Service.Level.PROJECT)
class TemplateParserService {

    fun getParser(type: TemplateType): ZwojeTemplateParser =
        when (type) {
            TemplateType.Thymeleaf -> ZwojeThymeleafTemplateParser
            TemplateType.Mustache -> ZwojeMustacheTemplateParser
            TemplateType.FreeMarker -> ZwojeFreemarkerTemplateParser
            TemplateType.Pebble -> ZwojePebbleTemplateParser
            TemplateType.GroovyTemplate -> ZwojeGroovyMarkupTemplateParser
            else -> stubTemplateParser
        }

    private companion object {
        val stubTemplateParser by lazy {
            object : ZwojeTemplateParser() {
                override fun parse(content: String): Set<TemplateVariable> = setOf()
            }
        }
    }
}