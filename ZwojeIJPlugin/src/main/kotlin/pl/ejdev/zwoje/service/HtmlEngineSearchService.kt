package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderEnumerator
import pl.ejdev.zwoje.core.template.TemplateType

@Service(Level.PROJECT)
class HtmlEngineSearchService @JvmOverloads constructor(
    private val project: Project,
    private val moduleManagerProvider: () -> ModuleManager = { ModuleManager.getInstance(project) },
    private val orderEnumeratorProvider: (Module) -> OrderEnumerator = { OrderEnumerator.orderEntries(it) }
) {
    private var moduleTemplates: Map<Module, TemplateType> = mapOf()

    fun getModuleTemplates() = moduleTemplates

    init {
        moduleTemplates = modulesAndDependencies()
            .asSequence()
            .map { (module, dependencies) -> module to findTemplateEngineFilesInRoots(dependencies) }
            .filter { (_, engine) -> engine.isNotEmpty() }
            .map { (name, engine) -> name to engine.first() }
            .associate { (name, engine) -> name to engine }
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
}

