package pl.ejdev.zwoje.service

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.openapi.roots.OrderRootsEnumerator
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import pl.ejdev.zwoje.core.template.TemplateType
import org.junit.Test

class HtmlEngineSearchServiceTest : BasePlatformTestCase() {

    private lateinit var mockProject: Project
    private lateinit var mockModuleManager: ModuleManager
    private lateinit var mockModule1: Module
    private lateinit var mockModule2: Module

    override fun setUp() {
        mockProject = mockk(relaxed = true)
        mockModuleManager = mockk()
        mockModule1 = mockk(relaxed = true)
        mockModule2 = mockk(relaxed = true)

        mockkStatic(ModuleManager::class)
        every { ModuleManager.getInstance(mockProject) } returns mockModuleManager
        mockkStatic(OrderEnumerator::class)
    }

    override fun tearDown() {
        try {
            unmockkAll()
        } catch (_: Exception) {
        }
    }

    @Test
    fun `initialization with no modules`() {
        every { mockModuleManager.modules } returns emptyArray()

        val service = HtmlEngineSearchService(mockProject)

        assertTrue(service.getModuleTemplates().isEmpty())
    }

    @Test
    fun `initialization with module containing Thymeleaf`() {
        setupModuleWithDependencies(mockModule1, listOf("lib/thymeleaf-3.0.jar"))
        every { mockModuleManager.modules } returns arrayOf(mockModule1)

        val service = HtmlEngineSearchService(mockProject)

        assertEquals(TemplateType.Thymeleaf, service.getModuleTemplates()[mockModule1])
    }

    @Test
    fun `initialization with module containing FreeMarker`() {
        setupModuleWithDependencies(mockModule1, listOf("lib/freemarker-2.3.jar"))
        every { mockModuleManager.modules } returns arrayOf(mockModule1)

        val service = HtmlEngineSearchService(mockProject)

        assertEquals(TemplateType.FreeMarker, service.getModuleTemplates()[mockModule1])
    }

    @Test
    fun `initialization with module containing Mustache`() {
        setupModuleWithDependencies(mockModule1, listOf("lib/mustache-1.0.jar"))
        every { mockModuleManager.modules } returns arrayOf(mockModule1)

        val service = HtmlEngineSearchService(mockProject)

        assertEquals(TemplateType.Mustache, service.getModuleTemplates()[mockModule1])
    }

    @Test
    fun `initialization with module containing Pebble`() {
        setupModuleWithDependencies(mockModule1, listOf("lib/pebble-3.1.jar"))
        every { mockModuleManager.modules } returns arrayOf(mockModule1)

        val service = HtmlEngineSearchService(mockProject)

        assertEquals(TemplateType.Pebble, service.getModuleTemplates()[mockModule1])
    }

    @Test
    fun `initialization with module containing GroovyTemplate`() {
        setupModuleWithDependencies(mockModule1, listOf("lib/groovy-templates-3.0.jar"))
        every { mockModuleManager.modules } returns arrayOf(mockModule1)

        val service = HtmlEngineSearchService(mockProject)

        assertEquals(TemplateType.GroovyTemplate, service.getModuleTemplates()[mockModule1])
    }

    @Test
    fun `initialization with module containing KotlinxHtml`() {
        setupModuleWithDependencies(mockModule1, listOf("lib/kotlinx-html-jvm-0.8.jar"))
        every { mockModuleManager.modules } returns arrayOf(mockModule1)

        val service = HtmlEngineSearchService(mockProject)

        assertEquals(TemplateType.KotlinxHtml, service.getModuleTemplates()[mockModule1])
    }

    @Test
    fun `initialization with multiple modules`() {
        setupModuleWithDependencies(mockModule1, listOf("lib/thymeleaf-3.0.jar"))
        setupModuleWithDependencies(mockModule2, listOf("lib/freemarker-2.3.jar"))
        every { mockModuleManager.modules } returns arrayOf(mockModule1, mockModule2)

        val service = HtmlEngineSearchService(mockProject)

        val templates = service.getModuleTemplates()
        assertEquals(2, templates.size)
        assertEquals(TemplateType.Thymeleaf, templates[mockModule1])
        assertEquals(TemplateType.FreeMarker, templates[mockModule2])
    }

    @Test
    fun `initialization with module containing multiple engines returns first found`() {
        setupModuleWithDependencies(
            mockModule1,
            listOf("lib/thymeleaf-3.0.jar", "lib/freemarker-2.3.jar")
        )
        every { mockModuleManager.modules } returns arrayOf(mockModule1)

        val service = HtmlEngineSearchService(mockProject)

        val templates = service.getModuleTemplates()
        assertEquals(1, templates.size)
        assertTrue(templates[mockModule1] in TemplateType.entries)
    }

    @Test
    fun `initialization with module containing no template engines`() {
        setupModuleWithDependencies(mockModule1, listOf("lib/spring-boot.jar"))
        every { mockModuleManager.modules } returns arrayOf(mockModule1)

        val service = HtmlEngineSearchService(mockProject)

        assertTrue(service.getModuleTemplates().isEmpty())
    }

    @Test
    fun `initialization with null paths`() {
        val mockEnumerator = mockk<OrderEnumerator>()
        val mockRootsEnumerator = mockk<OrderRootsEnumerator>()
        every { OrderEnumerator.orderEntries(mockModule1) } returns mockEnumerator
        every { mockEnumerator.classes() } returns mockRootsEnumerator
        every { mockRootsEnumerator.pathsList.pathList } returns listOf("lib/thymeleaf-3.0.jar", null)

        every { mockModuleManager.modules } returns arrayOf(mockModule1)

        val service = HtmlEngineSearchService(mockProject)

        assertEquals(TemplateType.Thymeleaf, service.getModuleTemplates()[mockModule1])
    }

    private fun setupModuleWithDependencies(module: Module, dependencies: List<String>) {
        val mockEnumerator = mockk<OrderEnumerator>()
        val mockRootsEnumerator = mockk<OrderRootsEnumerator>()

        every { OrderEnumerator.orderEntries(module) } returns mockEnumerator
        every { mockEnumerator.classes() } returns mockRootsEnumerator
        every { mockRootsEnumerator.pathsList.pathList } returns dependencies
    }
}
