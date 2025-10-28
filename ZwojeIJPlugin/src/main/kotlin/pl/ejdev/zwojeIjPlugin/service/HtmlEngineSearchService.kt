package pl.ejdev.zwojeIjPlugin.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderEnumerator
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplateResolver
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplateResolver
import java.io.File

@Service(Service.Level.PROJECT)
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

    private val zwojeThymeleafTemplateResolver = ZwojeThymeleafTemplateResolver()
    private val zwojeGroovyMarkupTemplateResolver = ZwojeGroovyMarkupTemplateResolver()

    fun templateResolvers() {
        findTemplateEngineFilesInRoots()

        moduleTemplates.values.map { template ->
            when (template) {
                TemplateType.Thymeleaf -> zwojeThymeleafTemplateResolver
                TemplateType.GroovyTemplate -> zwojeGroovyMarkupTemplateResolver
                TemplateType.Mustache -> TODO()
                TemplateType.FreeMarker -> TODO()
                TemplateType.KotlinxHtml -> TODO()
                TemplateType.Pebble -> TODO()
            }
        }
    }

    private fun findTemplateEngineFilesInRoots() {
        val moduleAndRoots = moduleTemplates
            .map { (module, _) -> module.name to ModuleRootManager.getInstance(module).contentRoots.map { it.path } }
            .toMap()
        moduleAndRoots
            .asSequence()
            .map { it.value.first() }
            .map { File(it) }
            .filter { it.exists() }
            .flatMap { it.listFiles().toList() }
            .filter { it.name.contains("resources") }
            .flatMap { it.listFiles().toList() }
            .filter { it.name.endsWith("templates") }
            .flatMap { it.listFiles().toList() }
            .toList()
    }

    private fun findTemplateEngineFilesInRoots(dependencies: List<String>): List<TemplateType> =
        dependencies.mapNotNull { TemplateType.entries.find { template -> it.contains(template.artifactName) } }

    fun modulesAndDependencies(): Map<Module, List<String>> =
        ModuleManager.getInstance(project).modules.associateWith { module ->
            OrderEnumerator.orderEntries(module)
                .classes()
                .pathsList
                .pathList
                .filterNotNull()
        }
}