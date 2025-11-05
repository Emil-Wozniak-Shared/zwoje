package pl.ejdev.zwoje.utils

import com.intellij.openapi.vfs.VirtualFile
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

private const val DEFAULT_EXTENSION = "html"
private const val DEFAULT_TEMPLATE_DIR = "templates"

internal fun VirtualFile.isSupported(resolver: ZwojeTemplateResolver<Any>): Boolean {
    val extension = when (resolver) {
        is TemplateProvider -> resolver.extension
        else -> DEFAULT_EXTENSION
    }

    val templateDir = when (resolver) {
        is TemplateProvider -> resolver.templatesDir
        else -> DEFAULT_TEMPLATE_DIR
    }

    return this.extension == extension && parent.path.endsWith(templateDir)
}

internal fun VirtualFile.nameWithExtension(extension: String): String =
    "${nameWithoutExtension}.${extension}"