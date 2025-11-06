package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplateResolver

@Service(Service.Level.PROJECT)
class TemplateParserService {

    fun getParser(resolver: ZwojeTemplateResolver<Any>): ZwojeTemplateParser<Any> =
        when (resolver.type) {
            TemplateType.Thymeleaf -> (resolver as ZwojeThymeleafTemplateResolver).getParser()
            else -> stubTemplateParser
        }

    private companion object {
        val stubTemplateParser by lazy {
            object : ZwojeTemplateParser<Any> {
                override fun parse(content: String): Set<TemplateVariable> = setOf()

            }
        }
    }
}