package pl.ejdev.zwoje.core.freemarker

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.shouldNotBe
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreeMarkerTemplate
import pl.ejdev.zwoje.core.template.freemarker.ZwojeFreeMarkerTemplateResolver

private const val TEMPLATE_NAME = "invoice"

class SampleTemplateInputData(input: InvoiceData) : TemplateInputData<InvoiceData>(input)

data class InvoiceData(val name: String, val amount: Double, val items: List<String>)

object InvoiceTemplate : ZwojeFreeMarkerTemplate<InvoiceData>(TEMPLATE_NAME)

class FreeMakerTemplateEngineSpec : FreeSpec({
    val compileEngine = OpenHtmlToPdfCompileEngine()
    val resolver = ZwojeFreeMarkerTemplateResolver()
    val engine = ZwojeEngine(compileEngine, resolver)
    resolver.register(TEMPLATE_NAME, InvoiceTemplate)

    "should compile FreeMaker based template" - {
        // given
        val data = InvoiceData("Alice", 199.99, listOf("Apples", "Eggs"))
        val input = SampleTemplateInputData(data)

        // when
        val bytes = engine.compile(TEMPLATE_NAME, input)

        // then
        bytes.size shouldNotBe 0

    }
})