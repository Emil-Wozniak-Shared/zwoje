package pl.ejdev.zwoje.service

import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldNotBe
import org.junit.Before
import org.junit.Test
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.TemplateType
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

private const val ID = "id"
private const val PATH = "path"
private const val BASE_DIR_PATH = "src/main/resources"

class TemplateResolverServiceTest {

    private lateinit var project: Project
    private lateinit var templateTypeService: TemplateTypeService
    private lateinit var htmlEngineSearchService: HtmlEngineSearchService
    private lateinit var service: TemplateResolverService

    @Before
    fun setUp() {
        mockkStatic("com.intellij.openapi.components.ServiceKt")
        project = mockk()
        templateTypeService = mockk()
        htmlEngineSearchService = mockk()

        every { project.service<TemplateTypeService>() } returns templateTypeService
        every { project.service<HtmlEngineSearchService>() } returns htmlEngineSearchService

        service = TemplateResolverService(project)
    }

    @Test
    fun `should register resolver when not already registered`() {
        val resolver = mockk<ZwojeTemplateResolver<Any>>(relaxed = true)
        val type = TemplateType.Thymeleaf
        every { resolver.type } returns type
        every { resolver.exists(ID) } returns false
        val template = TemplateTypeService.IJZwojeThymeleafTemplate(ID, PATH)
        every {
            templateTypeService.getTemplate(type, ID, PATH)
        } returns template

        service.register(resolver, ID, PATH)

        verify { templateTypeService.getTemplate(type, ID, PATH) }
        verify { resolver.register(ID, template) }
    }

    @Test
    fun `should return template resolvers list`() {
        val module1 = mockk<Module>()
        val module2 = mockk<Module>()

        val moduleTemplates = mapOf(
            module1 to TemplateType.Thymeleaf,
            module2 to TemplateType.Mustache
        )

        val resolvers = service.templateResolvers(moduleTemplates)

        resolvers.shouldHaveSize(2)
        resolvers.first().type shouldBe TemplateType.Thymeleaf
        resolvers.last().type shouldBe TemplateType.Mustache
    }

    @Test
    fun `should find resolver matching virtual file extension`() {
        val module = mockk<Module>()
        val file = mockk<VirtualFile>()
        every { file.extension } returns "html"

        val moduleTemplates = mapOf(module to TemplateType.Thymeleaf)
        every { htmlEngineSearchService.getModuleTemplates() } returns moduleTemplates

        val result = service.findFor(file)

        result shouldNotBe null
        result!!.type shouldBe TemplateType.Thymeleaf
    }

    @Test
    fun `should return null when no matching resolver found`() {
        val module = mockk<Module>()
        val file = mockk<VirtualFile>()
        every { file.extension } returns "xyz"

        val moduleTemplates = mapOf(module to TemplateType.Thymeleaf)
        every { htmlEngineSearchService.getModuleTemplates() } returns moduleTemplates

        val result = service.findFor(file)

        result shouldBe null
    }

    @Test
    fun `should create correct TemplateSpecification from provider`() {
        val provider = mockk<TemplateProvider>()
        every { provider.extension } returns "ftl"
        every { provider.templatesDir } returns "tmpl"
        every { provider.baseDir } returns BASE_DIR_PATH

        val spec = TemplateSpecification.of(provider)
        spec.ext shouldBe "ftl"
        spec.templatesDir shouldBe "tmpl"
        spec.baseDir shouldBe BASE_DIR_PATH
    }

    @Test
    fun `should use default TemplateSpecification`() {
        val default = TemplateSpecification.DEFAULT
        default.ext shouldBe "html"
        default.templatesDir shouldBe "templates"
        default.baseDir shouldBe BASE_DIR_PATH
    }
}
