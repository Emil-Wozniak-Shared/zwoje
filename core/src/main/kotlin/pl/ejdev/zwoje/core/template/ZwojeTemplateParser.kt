package pl.ejdev.zwoje.core.template

import org.jsoup.Jsoup

enum class VariableType { SINGLE, COLLECTION, OBJECT }

open class TemplateVariable(
    val name: String,
    val type: VariableType,
    val children: List<TemplateVariable>
)

abstract class ZwojeTemplateParser {
    abstract fun parse(content: String): Set<TemplateVariable>

    protected fun extract(
        content: String,
        apply: org.jsoup.nodes.Element.(MutableSet<TemplateVariable>) -> Unit
    ): Set<TemplateVariable> {
        val doc = Jsoup.parse(content)
        val variables = mutableSetOf<TemplateVariable>()
        doc.allElements.forEach { element ->
            apply(element, variables)
        }
        return variables
    }
}