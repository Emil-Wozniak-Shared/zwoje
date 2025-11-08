package pl.ejdev.zwoje.core.template.mustache

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import java.io.File
import java.io.StringWriter

abstract class ZwojeMustacheTemplate<INPUT : Any>(
    override val templatePath: String? = null
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {

    override fun compile(input: TemplateInputData<INPUT>): String {
        val mustache =
            if (isFullPath(templatePath!!)) compileFromFileSystem(templatePath!!)
            else compileFromClasspath(templatePath!!)

        val writer = StringWriter()
        mustache.execute(writer, input).flush()
        return writer.toString()
    }

    private fun isFullPath(path: String): Boolean = path.startsWith("/") ||
            path.startsWith("\\") ||
            path.matches(Regex("^[A-Za-z]:.*")) ||
            File(path).isAbsolute

    private fun compileFromFileSystem(fullPath: String): Mustache {
        val templateFile = File(fullPath)
        val mustacheFactory = DefaultMustacheFactory(templateFile.parentFile)
        return mustacheFactory.compile(templateFile.name)
    }

    private fun compileFromClasspath(resourcePath: String): Mustache {
        val mustacheFactory = DefaultMustacheFactory("templates")
        return mustacheFactory.compile(resourcePath)
    }
}