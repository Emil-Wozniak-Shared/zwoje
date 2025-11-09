package pl.ejdev.zwoje.core.template

enum class VariableType { SINGLE, COLLECTION, OBJECT }

open class TemplateVariable(
    val name: String,
    val type: VariableType,
    val children: List<TemplateVariable>
)

abstract class ZwojeTemplateParser {
    abstract fun parse(content: String): Set<TemplateVariable>
}