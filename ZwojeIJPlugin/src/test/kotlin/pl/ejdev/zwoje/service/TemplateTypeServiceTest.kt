package pl.ejdev.zwoje.service

import org.amshove.kluent.shouldNotBeNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pl.ejdev.zwoje.core.template.TemplateType.*

private const val TEST_ID = "test-id"
private const val PATH_TO_TEMPLATE = "/path/to/template"

class TemplateTypeServiceTest  {

    private lateinit var service: TemplateTypeService

    @Before
    fun setUp() {
        service = TemplateTypeService()
    }

    @Test
    fun `getTemplate returns Thymeleaf template`() {
        val template = service.getTemplate(Thymeleaf, TEST_ID, PATH_TO_TEMPLATE)

        template.shouldNotBeNull()
        assertTrue(template is TemplateTypeService.IJZwojeThymeleafTemplate)
    }

    @Test
    fun `getTemplate returns GroovyTemplate template`() {
        val template = service.getTemplate(GroovyTemplate, TEST_ID, PATH_TO_TEMPLATE)

        template.shouldNotBeNull()
        assertTrue(template is TemplateTypeService.IJZwojeGroovyMarkupTemplate)
    }

    @Test
    fun `getTemplate returns Mustache template`() {
        val template = service.getTemplate(Mustache, TEST_ID, PATH_TO_TEMPLATE)

        template.shouldNotBeNull()
        assertTrue(template is TemplateTypeService.IJZwojeMustacheTemplate)
    }

    @Test
    fun `getTemplate returns FreeMarker template`() {
        val template = service.getTemplate(FreeMarker, TEST_ID, PATH_TO_TEMPLATE)

        template.shouldNotBeNull()
        assertTrue(template is TemplateTypeService.IJZwojeFreeMarkerTemplate)
    }

    @Test
    fun `getTemplate returns Pebble template`() {
        val template = service.getTemplate(Pebble, TEST_ID, PATH_TO_TEMPLATE)

        template.shouldNotBeNull()
        assertTrue(template is TemplateTypeService.IJZwojePebbleTemplate)
    }

    @Test
    fun `getTemplate with different ids`() {
        val id1 = "template-1"
        val id2 = "template-2"
        val path = "/templates/test.html"

        val template1 = service.getTemplate(Thymeleaf, id1, path)
        val template2 = service.getTemplate(Thymeleaf, id2, path)

        template1.shouldNotBeNull()
        template2.shouldNotBeNull()
        assertNotSame(template1, template2)
    }

    @Test
    fun `getTemplate with different paths`() {
        val id = TEST_ID
        val path1 = "/templates/test1.html"
        val path2 = "/templates/test2.html"

        val template1 = service.getTemplate(Mustache, id, path1)
        val template2 = service.getTemplate(Mustache, id, path2)

        template1.shouldNotBeNull()
        template2.shouldNotBeNull()
        assertNotSame(template1, template2)
    }

    @Test
    fun `all template types except KotlinxHtml return non-null`() {
        val types = listOf(
            Thymeleaf,
            GroovyTemplate,
            Mustache,
            FreeMarker,
            Pebble
        )

        types.forEach { type ->
            val template = service.getTemplate(type, TEST_ID, PATH_TO_TEMPLATE)
            template.shouldNotBeNull()
        }
    }
}