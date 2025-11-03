package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreeMarkerTemplate
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplate
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplate
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplate
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplate
import pl.ejdev.zwoje.service.OpenHtmlEngineCompileService.IJTemplateInputData

@Service(Service.Level.PROJECT)
class TemplateTypeService {

    fun getTemplate(type: TemplateType, id: String, templatePath: String) = when (type) {
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

}