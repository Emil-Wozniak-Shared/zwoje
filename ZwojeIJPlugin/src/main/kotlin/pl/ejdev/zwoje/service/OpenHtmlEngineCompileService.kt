package pl.ejdev.zwoje.service

import com.google.gson.Gson
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages.showErrorDialog
import com.intellij.openapi.ui.Messages.showWarningDialog
import com.intellij.openapi.vfs.VirtualFile
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import pl.ejdev.zwoje.service.ZwojeSampleService.GetSampleResult
import kotlin.Result.Companion.failure

private const val TITLE = "OpenHtmlEngineCompileService"

@Service(Service.Level.PROJECT)
class OpenHtmlEngineCompileService(
    private val project: Project
) {
    private val templateResolverService = project.service<TemplateResolverService>()
    private val zwojeSampleService = project.service<ZwojeSampleService>()
    private val compileEngine = OpenHtmlToPdfCompileEngine()
    private val jsonParseService = project.service<JsonParseService>()

    private val engines: MutableMap<TemplateType, ZwojeEngine> = mutableMapOf()

    fun compile(resolver: ZwojeTemplateResolver<Any>, file: VirtualFile, content: String): Result<ByteArray> {
        val id = file.name
        templateResolverService.register(resolver, id, file.path)
        val result = zwojeSampleService.getSamples(file)
        when (result) {
            is GetSampleResult.OK -> {
                return findEngine(resolver).runCatching { compile(id, inputData(result.content)) }
            }

            is GetSampleResult.FileNotExists ->
                showWarningDialog(project, "Sample file for ${file.name} does not exist.", TITLE)

            is GetSampleResult.Error ->
                showErrorDialog(project, "Load samples failed ${result.message}.", TITLE)
        }
        return failure(RuntimeException("Failed to compile $result"))
    }

    private fun inputData(result: String): IJTemplateInputData = when (val data = parseJsonFileToObject(result)) {
        null -> IJTemplateInputData(listOf<Any>())
        else -> IJTemplateInputData(data.first())
    }

    private fun findEngine(resolver: ZwojeTemplateResolver<Any>): ZwojeEngine {
        var engine = engines[resolver.type]
        if (engine == null) {
            engine = ZwojeEngine(compileEngine, resolver)
            engines[resolver.type] = engine
        }
        return engine
    }

    private fun parseJsonFileToObject(content: String): List<Any>? =
        jsonParseService.parse<Data>(content).samples.takeIf { it.isNotEmpty() }

    class IJTemplateInputData(input: Any) : TemplateInputData<Any>(input)

    class Data(
        val samples: List<Any>
    )
}