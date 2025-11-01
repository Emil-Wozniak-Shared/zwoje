package pl.ejdev.zwoje.core.template.mustache

import com.github.mustachejava.DefaultMustacheFactory
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import java.io.InputStreamReader
import java.io.StringWriter
import kotlin.text.Charsets.UTF_8

abstract class ZwojeMustacheTemplate<INPUT : Any>(
    private val resourcePath: String
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {

    private val mustacheFactory = DefaultMustacheFactory()

    override fun compile(input: TemplateInputData<INPUT>): String {
        val resourceStream = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Template not found at path: $resourcePath")

        val reader = InputStreamReader(resourceStream, UTF_8)
        val mustache = mustacheFactory.compile(reader, resourcePath)
        val writer = StringWriter()
        mustache.execute(writer, input.data).flush()
        return writer.toString()
    }
}
