package pl.ejdev.zwoje.core.template

enum class VariableType { SINGLE, COLLECTION, OBJECT }

open class TemplateVariable(
    val name: String,
    val type: VariableType
)

interface ZwojeTemplateParser<INPUT : Any> {
    fun parse(content: String): Set<TemplateVariable>
}