package pl.ejdev.zwoje.core.pebble

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.shouldNotBe
import pl.ejdev.zwoje.core.ZwojeEngine
import pl.ejdev.zwoje.core.common.InvoiceData
import pl.ejdev.zwoje.core.common.SampleTemplateInputData
import pl.ejdev.zwoje.core.common.TEMPLATE_NAME
import pl.ejdev.zwoje.core.engine.OpenHtmlToPdfCompileEngine
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplate
import pl.ejdev.zwoje.core.template.pebble.ZwojePebbleTemplateResolver

object InvoiceTemplate : ZwojePebbleTemplate<InvoiceData>(TEMPLATE_NAME)

class PebbleTemplateEngineSpec : FreeSpec({
    val compileEngine = OpenHtmlToPdfCompileEngine()
    val resolver = ZwojePebbleTemplateResolver()
    val engine = ZwojeEngine(compileEngine, resolver)
    resolver.register(TEMPLATE_NAME, InvoiceTemplate)

    "should compile Pebble based template" - {
        // given
        val data = InvoiceData("Alice", 199.99, listOf("Apples", "Eggs"))
        val input = SampleTemplateInputData(data)

        // when
        val bytes = engine.compile(TEMPLATE_NAME, input)

        // then
        bytes.size shouldNotBe 0

    }
})