package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreeMarkerTemplate
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplate
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplate
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplate
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplate
import pl.ejdev.zwoje.service.OpenHtmlEngineCompileService.IJTemplateInputData

@Service(Service.Level.PROJECT)
class TemplateTypeService {

    fun getTemplate(
        type: TemplateType,
        id: String,
        templatePath: String
    ): ZwojeTemplate<TemplateInputData<IJTemplateInputData>, IJTemplateInputData> = when (type) {
        TemplateType.Thymeleaf -> IJZwojeThymeleafTemplate(id, templatePath)
        TemplateType.GroovyTemplate -> IJZwojeGroovyMarkupTemplate(id, templatePath)
        TemplateType.Mustache -> IJZwojeMustacheTemplate(id, templatePath)
        TemplateType.FreeMarker -> IJZwojeFreeMarkerTemplate(id, templatePath)
        TemplateType.Pebble -> IJZwojePebbleTemplate(id, templatePath)
        TemplateType.KotlinxHtml -> TODO("kotlinx html is not ready")
    }

    class IJZwojeThymeleafTemplate(name: String, templatePath: String) :
        ZwojeThymeleafTemplate<IJTemplateInputData>(name, templatePath)

    class IJZwojeGroovyMarkupTemplate(name: String, templatePath: String) :
        ZwojeGroovyMarkupTemplate<IJTemplateInputData>(templatePath)

    class IJZwojeMustacheTemplate(name: String, templatePath: String) :
        ZwojeMustacheTemplate<IJTemplateInputData>(templatePath)

    class IJZwojeFreeMarkerTemplate(name: String, templatePath: String) :
        ZwojeFreeMarkerTemplate<IJTemplateInputData>(name, templatePath)

    class IJZwojePebbleTemplate(name: String, templatePath: String) :
        ZwojePebbleTemplate<IJTemplateInputData>(templatePath)

}