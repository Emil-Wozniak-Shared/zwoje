package pl.ejdev.zwoje.core.template

abstract class TemplateInputData<INPUT: Any>(
    val data: INPUT
)

interface ZwojeTemplate<out TD: TemplateInputData<INPUT>,  INPUT : Any> {
    fun compile(input: @UnsafeVariance TD): String
}
