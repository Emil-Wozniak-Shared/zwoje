package pl.ejdev.zwoje.core.mustache

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.shouldNotBe
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.common.InvoiceData
import pl.ejdev.zwoje.core.common.SampleTemplateInputData
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplate
import pl.ejdev.zwoje.core.template.mustache.ZwojeMustacheTemplateResolver

object InvoiceTemplate : ZwojeMustacheTemplate<InvoiceData>("invoice.mustache")

private const val TEMPLATE_NAME = "invoice"

class MustacheTemplateEngineSpec : FreeSpec({
    val compileEngine = OpenHtmlToPdfCompileEngine()
    val resolver = ZwojeMustacheTemplateResolver()
    val engine = ZwojeEngine(compileEngine, resolver)
    resolver.register(TEMPLATE_NAME, InvoiceTemplate)

    "should compile mustache based template" - {
        // given
        val input = SampleTemplateInputData(InvoiceData("Alice", 199.99, listOf()))

        // when
        val bytes = engine.compile(TEMPLATE_NAME, input)

        // then
        bytes.size shouldNotBe 0

    }
})