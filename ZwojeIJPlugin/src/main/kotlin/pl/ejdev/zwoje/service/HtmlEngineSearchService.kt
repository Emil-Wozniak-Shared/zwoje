package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderEnumerator
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreeMarkerTemplateResolver
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplateResolver
import pl.ejdev.zwoje.core.template.kotlinx.ZwojeKotlinHtmlTemplateResolver
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplateResolver
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplateResolver
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplateResolver
import java.io.File

private const val RESOURCES = "resources"
private const val TEMPLATES = "templates"

@Service(Level.PROJECT)
class HtmlEngineSearchService(
    private val project: Project
) {
    private var moduleTemplates: Map<Module, TemplateType> = mapOf()

    init {
        moduleTemplates = modulesAndDependencies()
            .asSequence()
            .map { (module, dependencies) -> module to findTemplateEngineFilesInRoots(dependencies) }
            .filter { (_, engine) -> engine.isNotEmpty() }
            .map { (name, engine) -> name to engine.first() }
            .associate { (name, engine) -> name to engine }
    }

    fun templateResolvers() {
        moduleTemplates.values
            .map { template ->
                when (template) {
                    TemplateType.Thymeleaf -> zwojeThymeleafTemplateResolver
                    TemplateType.GroovyTemplate -> zwojeGroovyMarkupTemplateResolver
                    TemplateType.Mustache -> zwojeMustacheTemplateResolver
                    TemplateType.FreeMarker -> zwojeFreeMakerTemplateResolver
                    TemplateType.KotlinxHtml -> zwojeKotlinHtmlTemplateResolver
                    TemplateType.Pebble -> zwojePebbleTemplateResolver
                }
            }
            .also {
                when (it) {
                    is TemplateProvider -> TemplateSpec(it.extension, it.templatePath)
                    else -> TemplateSpec(DEFAULT_EXT, DEFAULT_PATH)
                }.let(::findTemplateEngineFilesInRoots)
            }
    }

    private fun findTemplateEngineFilesInRoots(specification: TemplateSpec) {
        val moduleAndRoots = moduleTemplates
            .map { (module, _) -> module.name to ModuleRootManager.getInstance(module).contentRoots.map { it.path } }
            .toMap()
        moduleAndRoots
            .asSequence()
            .filter { it.value.isNotEmpty() }
            .map { it.value.first() }
            .map { File(it) }
            .filter { it.exists() }
            .flatMap { it.listFiles().toList() }
            .filter { it.name == specification.path }
            .flatMap { it.listFiles().toList() }
            .filter { it.name.endsWith(TEMPLATES) }
            .flatMap { it.listFiles().toList() }
            .toList()
    }

    private fun findTemplateEngineFilesInRoots(dependencies: List<String>): List<TemplateType> =
        dependencies.mapNotNull { TemplateType.entries.find { template -> it.contains(template.artifactName) } }

    private fun modulesAndDependencies(): Map<Module, List<String>> =
        ModuleManager.getInstance(project).modules.associateWith { module ->
            OrderEnumerator.orderEntries(module)
                .classes()
                .pathsList
                .pathList
                .filterNotNull()
        }

    private data class TemplateSpec(
        val ext: String,
        val path: String
    )

    private companion object {
        private val zwojeThymeleafTemplateResolver by lazy { ZwojeThymeleafTemplateResolver() }
        private val zwojeGroovyMarkupTemplateResolver by lazy { ZwojeGroovyMarkupTemplateResolver() }
        private val zwojeMustacheTemplateResolver by lazy { ZwojeMustacheTemplateResolver() }
        private val zwojeFreeMakerTemplateResolver by lazy { ZwojeFreeMarkerTemplateResolver() }
        private val zwojeKotlinHtmlTemplateResolver by lazy { ZwojeKotlinHtmlTemplateResolver() }
        private val zwojePebbleTemplateResolver by lazy { ZwojePebbleTemplateResolver() }

        private const val DEFAULT_EXT = "html"
        private const val DEFAULT_PATH = "src/main/resources/templates/"
    }

}