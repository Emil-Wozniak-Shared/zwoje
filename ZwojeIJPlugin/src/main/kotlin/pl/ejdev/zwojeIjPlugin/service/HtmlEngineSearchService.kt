package pl.ejdev.zwojeIjPlugin.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderEnumerator
import pl.ejdev.zwoje.core.template.Template
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplateResolver
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplateResolver

@Service(Service.Level.PROJECT)
class HtmlEngineSearchService(
    private val project: Project
) {
    private var moduleTemplates: Map<Module, Template> = mapOf()
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
                Template.Thymeleaf -> zwojeThymeleafTemplateResolver
                Template.GroovyTemplate -> zwojeGroovyMarkupTemplateResolver
                Template.Mustache -> TODO()
                Template.FreeMarker -> TODO()
                Template.KotlinxHtml -> TODO()
                Template.Pebble -> TODO()
            }
        }
    }

    private fun findTemplateEngineFilesInRoots() {
        val moduleAndRoots = moduleTemplates
            .map { (module, _) -> module.name to ModuleRootManager.getInstance(module).contentRoots.map { it.path } }
            .toMap()
    }

    private fun findTemplateEngineFilesInRoots(dependencies: List<String>): List<Template> =
        dependencies.mapNotNull { Template.entries.find { template -> it.contains(template.artifactName) } }

    fun modulesAndDependencies(): Map<Module, List<String>> =
        ModuleManager.getInstance(project).modules.associateWith { module ->
            OrderEnumerator.orderEntries(module)
                .classes()
                .pathsList
                .pathList
                .filterNotNull()
        }
}