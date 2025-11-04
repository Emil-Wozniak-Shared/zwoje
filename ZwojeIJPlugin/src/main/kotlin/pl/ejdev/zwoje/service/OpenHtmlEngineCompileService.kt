package pl.ejdev.zwoje.service

import com.google.gson.Gson
import com.intellij.json.JsonFileType
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages.showErrorDialog
import com.intellij.openapi.ui.Messages.showWarningDialog
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

private const val TITLE = "OpenHtmlEngineCompileService"

@Service(Service.Level.PROJECT)
class OpenHtmlEngineCompileService(
    private val project: Project
) {
    private val templateResolverService = project.service<TemplateResolverService>()
    private val templateParserService = project.service<TemplateParserService>()
    private val zwojeSampleService = project.service<ZwojeSampleService>()
    private val compileEngine = OpenHtmlToPdfCompileEngine()
    private val gson = Gson()

    private val engines: MutableMap<TemplateType, ZwojeEngine> = mutableMapOf()

    fun compile(resolver: ZwojeTemplateResolver<Any>, file: VirtualFile, content: String): Result<ByteArray> {
        val id = file.name
        templateResolverService.register(resolver, id, file.path)
        val variables = templateParserService.getParser(id, resolver).parse(content)
        when (val result = zwojeSampleService.getSamples(file)) {
            is ZwojeSampleService.GetSampleResult.OK -> {
                val input = inputData(result.content)
                val bytes = findEngine(resolver).compile(id, input)
                return success(bytes)
            }

            is ZwojeSampleService.GetSampleResult.FileNotExists ->
                showWarningDialog(project, "Sample file for ${file.name} does not exist.", TITLE)

            is ZwojeSampleService.GetSampleResult.Error ->
                showErrorDialog(project, "Load samples failed ${result.message}.", TITLE)
        }
        return failure(RuntimeException("Failed to compile"))
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

    private fun parseJsonFileToObject(content: String): List<Any>? {
        val json = gson.fromJson(content, Data::class.java)
        return json.samples.takeIf { it.isNotEmpty() }
    }

    class IJTemplateInputData(input: Any) : TemplateInputData<Any>(input)

    class Data(
        val samples: List<Any>
    )
}