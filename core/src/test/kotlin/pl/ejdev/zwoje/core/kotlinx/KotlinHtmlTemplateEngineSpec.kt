package pl.ejdev.zwoje.core.kotlinx

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.shouldNotBe
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine
import pl.ejdev.zwoje.core.template.kotlinx.ZwojeKotlinHtmlTemplateResolver

private const val TEMPLATE_NAME = "users"

class KotlinHtmlTemplateEngineSpec : FreeSpec({

    val compileEngine = OpenHtmlToPdfCompileEngine()
    val resolver = ZwojeKotlinHtmlTemplateResolver()
    val engine = ZwojeEngine(compileEngine, resolver)
    resolver.register(TEMPLATE_NAME, SampleZwojeKotlinHtmlTemplate)

    "should compile kotlinx html based template" -  {
        // given
        val sampleTemplateData = SampleTemplateInputData(listOf("Emil", "Ola"))

        // when
        val bytes = engine.compile(TEMPLATE_NAME, sampleTemplateData)

        // then
        bytes.size shouldNotBe 0

    }
})