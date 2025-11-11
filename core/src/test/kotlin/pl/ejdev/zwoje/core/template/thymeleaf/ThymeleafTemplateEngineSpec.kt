package pl.ejdev.zwoje.core.template.thymeleaf

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.shouldNotBe
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.common.InvoiceData
import pl.ejdev.zwoje.core.common.SampleTemplateInputData
import pl.ejdev.zwoje.core.common.TEMPLATE_NAME
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine

object InvoiceTemplate : ZwojeThymeleafTemplate<InvoiceData>(TEMPLATE_NAME, null)

class ThymeleafTemplateEngineSpec : FreeSpec({
    val compileEngine = OpenHtmlToPdfCompileEngine()
    val resolver = ZwojeThymeleafTemplateResolver()
    val engine = ZwojeEngine(compileEngine, resolver)
    resolver.register(TEMPLATE_NAME, InvoiceTemplate)

    "should compile thymeleaf based template" - {
        // given
        val data = InvoiceData("Alice", 199.99, listOf("Apples", "Eggs"))
        val input = SampleTemplateInputData(data)

        // when
        val bytes = engine.compile(TEMPLATE_NAME, input)

        // then
        bytes.size shouldNotBe 0

    }
})