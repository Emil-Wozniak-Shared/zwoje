package pl.ejdev.zwoje.core.template

import net.sf.jasperreports.engine.JasperPrint

abstract class TemplateInputData<INPUT: Any>(
    val data: INPUT
)

interface ZwojeTemplate<out TD: TemplateInputData<INPUT>,  INPUT : Any> {
    fun compile(input: @UnsafeVariance TD): TemplateOutput
    val templatePath: String?
}

sealed class TemplateOutput {
    class Html(val html: String) : TemplateOutput()
    class JasperFill(val print: JasperPrint) : TemplateOutput()
}