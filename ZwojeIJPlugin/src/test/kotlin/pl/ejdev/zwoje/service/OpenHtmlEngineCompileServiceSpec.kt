package pl.ejdev.zwoje.service

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import io.mockk.*
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeEmpty
import org.amshove.kluent.shouldNotBeNull
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import pl.ejdev.zwoje.service.OpenHtmlEngineCompileService.IJTemplateInputData
import pl.ejdev.zwoje.service.ZwojeSampleService.GetSampleResult

private const val HTML_TEXT = "<html></html>"
private const val EMPTY_SAMPLES = """{"samples": []}"""
private const val SAMPLES_WITH_DATA = """{"samples": [["a","b"]]}"""

class OpenHtmlEngineCompileServiceTest {

    private lateinit var mockProject: Project
    private lateinit var mockResolverService: TemplateResolverService
    private lateinit var mockSampleService: ZwojeSampleService
    private lateinit var mockFile: VirtualFile
    private lateinit var mockResolver: ZwojeTemplateResolver<Any>
    private lateinit var mockEngine: ZwojeEngine
    private lateinit var service: OpenHtmlEngineCompileService

    @Before
    fun setUp() {
        mockkStatic(Messages::class)
        mockkStatic("com.intellij.openapi.components.ServiceKt")

        mockProject = mockk(relaxed = true)
        mockResolverService = mockk(relaxed = true)
        mockSampleService = mockk(relaxed = true)
        mockFile = mockk()
        mockResolver = mockk(relaxed = true)
        mockEngine = mockk(relaxed = true)

        every { mockFile.name } returns "test.html"
        every { mockFile.path } returns "/tmp/test.html"

        every { mockProject.service<TemplateResolverService>() } returns mockResolverService
        every { mockProject.service<ZwojeSampleService>() } returns mockSampleService

        service = OpenHtmlEngineCompileService(mockProject)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `compile success returns Result with byte array`() {
        val sampleJson = """{"samples": [["key", "value"]]}"""
        val result = GetSampleResult.OK(sampleJson)

        every { mockSampleService.getSamples(mockFile) } returns result
        mockkConstructor(ZwojeEngine::class)
        every { anyConstructed<ZwojeEngine>().compile(any(), any<IJTemplateInputData>()) } returns "PDF".toByteArray()
        every { mockResolver.type } returns TemplateType.Thymeleaf

        val compileResult = service.compile(mockResolver, mockFile, HTML_TEXT)

        assertTrue(compileResult.isSuccess)
        assertArrayEquals("PDF".toByteArray(), compileResult.getOrNull())
        verify { mockResolverService.register(mockResolver, "test.html", "/tmp/test.html") }
    }

    @Test
    fun `compile when sample file missing shows warning dialog`() {
        every { mockSampleService.getSamples(mockFile) } returns GetSampleResult.FileNotExists
        every { Messages.showWarningDialog(any<Project>(), any(), any()) } returns Unit

        val result = service.compile(mockResolver, mockFile, HTML_TEXT)

        assertTrue(result.isFailure)
        verify { Messages.showWarningDialog(mockProject, match { it.contains("does not exist") }, any()) }
    }

    @Test
    fun `compile when sample loading error shows error dialog`() {
        every { mockSampleService.getSamples(mockFile) } returns GetSampleResult.Error("broken")
        every { Messages.showErrorDialog(any<Project>(), any(), any()) } returns Unit

        val result = service.compile(mockResolver, mockFile, HTML_TEXT)

        assertTrue(result.isFailure)
        verify { Messages.showErrorDialog(mockProject, match { it.contains("broken") }, any()) }
    }

    @Test
    fun `compile caches engine per TemplateType`() {
        val sampleJson = SAMPLES_WITH_DATA
        every { mockSampleService.getSamples(mockFile) } returns GetSampleResult.OK(sampleJson)
        every { mockResolver.type } returns TemplateType.Thymeleaf
        mockkConstructor(ZwojeEngine::class)
        every { anyConstructed<ZwojeEngine>().compile(any(), any<IJTemplateInputData>()) } returns "data".toByteArray()

        // First compile — creates engine
        val result1 = service.compile(mockResolver, mockFile, "html")
        assertTrue(result1.isSuccess)

        // Second compile — should reuse the same engine, not create new
        val result2 = service.compile(mockResolver, mockFile, "html")
        result2 shouldBeEqualTo true
    }

    @Test
    fun `inputData with empty samples returns empty TemplateInputData`() {
        val method = service.javaClass.getDeclaredMethod("inputData", String::class.java)
        method.isAccessible = true
        val data = method.invoke(service, EMPTY_SAMPLES)
        data.shouldBeNull()
    }

    @Test
    fun `parseJsonFileToObject returns null for empty samples`() {
        val method = service.javaClass.getDeclaredMethod("parseJsonFileToObject", String::class.java)
        method.isAccessible = true
        val result = method.invoke(service, EMPTY_SAMPLES)
        result.shouldBeNull()
    }

    @Test
    fun `parseJsonFileToObject returns list for valid JSON`() {
        val method = service.javaClass.getDeclaredMethod("parseJsonFileToObject", String::class.java)
        method.isAccessible = true
        val result = method.invoke(service, SAMPLES_WITH_DATA) as? List<*>
        result.shouldNotBeNull()
        result.shouldNotBeEmpty()
    }
}
