package pl.ejdev.zwoje.service

import org.amshove.kluent.`should be`
import org.junit.Before
import org.junit.Test
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreemarkerTemplateParser
import pl.ejdev.zwoje.core.template.groovyTemplates.ZwojeGroovyMarkupTemplateParser
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplateParser
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplateParser
import pl.ejdev.zwoje.core.template.thymeleaf.ZwojeThymeleafTemplateParser

class TemplateParserServiceTest {

    private lateinit var service: TemplateParserService

    @Before
    fun setUp() {
        service = TemplateParserService()
    }

    @Test
    fun `should return Thymeleaf parser`() {
        val parser = service.getParser(TemplateType.Thymeleaf)
        parser `should be`  ZwojeThymeleafTemplateParser
    }

    @Test
    fun `should return Mustache parser`() {
        val parser = service.getParser(TemplateType.Mustache)
        parser `should be`  ZwojeMustacheTemplateParser
    }

    @Test
    fun `should return FreeMarker parser`() {
        val parser = service.getParser(TemplateType.FreeMarker)
        parser `should be`  ZwojeFreemarkerTemplateParser
    }

    @Test
    fun `should return Pebble parser`() {
        val parser = service.getParser(TemplateType.Pebble)
        parser `should be`  ZwojePebbleTemplateParser
    }

    @Test
    fun `should return GroovyTemplate parser`() {
        val parser = service.getParser(TemplateType.GroovyTemplate)
        parser `should be`  ZwojeGroovyMarkupTemplateParser
    }

    @Test
    fun `should return stub parser for unmapped TemplateType`() {
        val parser = service.getParser(TemplateType.KotlinxHtml)

        parser `should be` TemplateParserService.stubTemplateParser
    }

}
