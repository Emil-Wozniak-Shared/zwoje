package pl.ejdev.zwoje.core.template.thymeleaf

import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser

class ThymeleafVariable(
    name: String,
    type: VariableType
) : TemplateVariable(name, type)

object ZwojeThymeleafTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> = extract(content) { variables ->
        thymeleafAttrs
            .filter { attr(it).isNotBlank() }
            .forEach { text ->
                // Extract all expressions like ${...}, #{...}, @{...}
                extractExpressions(attr(text))
                    .filter { (_, expr) -> expr !in variables.map { it.name } }
                    .forEach { (prefix, expr) ->
                        val type = detectType(prefix, expr)
                        variables.add(ThymeleafVariable(expr, type))
                    }
            }
    }

    /**
     * Extracts expressions of form ${...}, #{...}, @{...} without regex.
     * Returns a list of pairs (prefix, content)
     */
    private fun extractExpressions(value: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        val prefixes = listOf("\${", "#{", "@{")
        var i = 0

        while (i < value.length) {
            val startIndex = prefixes
                .map { prefix -> prefix to value.indexOf(prefix, i) }
                .filter { it.second != -1 }
                .minByOrNull { it.second } ?: break

            val (prefix, start) = startIndex
            val end = value.indexOf('}', start)
            if (end == -1) break

            val inner = value.substring(start + prefix.length, end).trim()
            results.add(prefix to inner)
            i = end + 1
        }

        return results
    }

    private fun detectType(prefix: String, expression: String): VariableType {
        return when (prefix) {
            "#{", "@{" -> VariableType.SINGLE // i18n and URL expressions are usually single values
            else -> when {
                expression.contains(" in ") -> VariableType.COLLECTION
                expression.contains('.') || expression.contains("?.") -> VariableType.OBJECT
                else -> VariableType.SINGLE
            }
        }
    }

    private val thymeleafAttrs = listOf(
        "th:text", "th:each", "th:if", "th:unless",
        "th:value", "th:with", "th:attr"
    )

}