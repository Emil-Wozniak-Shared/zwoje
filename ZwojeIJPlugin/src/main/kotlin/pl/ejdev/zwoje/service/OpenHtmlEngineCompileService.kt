package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

@Service(Service.Level.PROJECT)
class OpenHtmlEngineCompileService(
    private val project: Project
) {
    private val templateParserService = project.service<TemplateParserService>()
    private val templateTypeService = project.service<TemplateTypeService>()
    private val compileEngine = OpenHtmlToPdfCompileEngine()

    fun compile(resolver: ZwojeTemplateResolver<Any>, id: String, templatePath: String, content: String): ByteArray {
        val templateType = resolver.type
        val engine = ZwojeEngine(compileEngine, resolver)
        val template = templateTypeService.getTemplate(templateType, id, templatePath)
        if (!resolver.exists(id)) {
            resolver.register(id, template)
        }
        val parser = templateParserService.getParser(id, resolver)
        val parsed = parser.parse(content)
        val input = IJTemplateInputData(parsed)
        val bytes = engine.compile(id, input)
        return bytes
    }

    class IJTemplateInputData(input: Any) : TemplateInputData<Any>(input)

}