package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreeMarkerTemplateResolver
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplateResolver
import pl.ejdev.zwoje.core.template.jasper.JasperTemplateResolver
import pl.ejdev.zwoje.core.template.kotlinx.ZwojeKotlinHtmlTemplateResolver
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplateResolver
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplateResolver
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplateResolver

@Service(Level.PROJECT)
class TemplateResolverService(
    private val project: Project
) {
    private val templateTypeService = project.service<TemplateTypeService>()
    private val htmlEngineSearchService = this.project.service<HtmlEngineSearchService>()

    fun templateResolvers(moduleTemplates: Map<Module, TemplateType>): List<ZwojeTemplateResolver<Any>> =
        moduleTemplates.values.map { type -> type.toTemplateResolver() }

    fun<T: Any> toSpecification(resolver: ZwojeTemplateResolver<T>): TemplateSpecification = when (resolver) {
        is TemplateProvider -> TemplateSpecification.of(resolver)
        else -> TemplateSpecification.DEFAULT
    }

    fun register(resolver: ZwojeTemplateResolver<Any>, id: String, templatePath: String) {
        val template = templateTypeService.getTemplate(resolver.type, id, templatePath)
        if (!resolver.exists(id)) {
            resolver.register(id, template)
        }
    }

    fun findFor(virtualFile: VirtualFile): ZwojeTemplateResolver<Any>? {
        val moduleTemplates = htmlEngineSearchService.getModuleTemplates()
        val templateResolvers = templateResolvers(moduleTemplates)
        return templateResolvers
            .asSequence()
            .filter { if (it is TemplateProvider) it.extension == virtualFile.extension else true }
            .distinct()
            .firstOrNull()
    }

    private fun TemplateType.toTemplateResolver(): ZwojeTemplateResolver<Any> = when (this) {
        TemplateType.Thymeleaf -> zwojeThymeleafTemplateResolver
        TemplateType.GroovyTemplate -> zwojeGroovyMarkupTemplateResolver
        TemplateType.Mustache -> zwojeMustacheTemplateResolver
        TemplateType.FreeMarker -> zwojeFreeMakerTemplateResolver
        TemplateType.KotlinxHtml -> zwojeKotlinHtmlTemplateResolver
        TemplateType.Pebble -> zwojePebbleTemplateResolver
        TemplateType.Jasper -> zwojeJasperTemplateResolver
    }

    private companion object {
        private val zwojeThymeleafTemplateResolver by lazy { ZwojeThymeleafTemplateResolver() }
        private val zwojeGroovyMarkupTemplateResolver by lazy { ZwojeGroovyMarkupTemplateResolver() }
        private val zwojeMustacheTemplateResolver by lazy { ZwojeMustacheTemplateResolver() }
        private val zwojeFreeMakerTemplateResolver by lazy { ZwojeFreeMarkerTemplateResolver() }
        private val zwojeKotlinHtmlTemplateResolver by lazy { ZwojeKotlinHtmlTemplateResolver() }
        private val zwojePebbleTemplateResolver by lazy { ZwojePebbleTemplateResolver() }
        private val zwojeJasperTemplateResolver by lazy { JasperTemplateResolver() }
    }
}

data class TemplateSpecification(
    val ext: String,
    val templatesDir: String,
    val baseDir: String
) {

    companion object {
        private const val DEFAULT_EXT = "html"
        private const val DEFAULT_RESOURCES = "src/main/resources"
        private const val DEFAULT_TEMPLATES = "templates"

        val DEFAULT = TemplateSpecification(DEFAULT_EXT, DEFAULT_TEMPLATES, DEFAULT_RESOURCES)

        fun of(provider: TemplateProvider): TemplateSpecification =
            TemplateSpecification(
                ext = provider.extension,
                templatesDir = provider.templatesDir,
                baseDir = provider.baseDir
            )
    }
}