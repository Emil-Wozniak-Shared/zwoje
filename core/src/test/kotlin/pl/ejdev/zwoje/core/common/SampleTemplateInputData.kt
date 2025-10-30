package pl.ejdev.zwoje.core.common

import pl.ejdev.zwoje.core.template.TemplateInputData

const val TEMPLATE_NAME = "invoice"

class SampleTemplateInputData(
    input: InvoiceData
) : TemplateInputData<InvoiceData>(input)
data class InvoiceData(
    val name: String,
    val amount: Double,
    val items: List<String>
)
