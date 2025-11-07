package pl.ejdev.zwoje.core.template.pebble

import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser

class PebbleVariable(
    name: String,
    type: VariableType
) : TemplateVariable(name, type)

object ZwojePebbleTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> =
        extract(content) { variables ->
            val text = this.outerHtml()

            // Handle {{ ... }} expressions
            extractExpressions(text)
                .filter { it !in variables.map { v -> v.name } }
                .forEach { expr ->
                    val type = detectType(expr)
                    variables.add(PebbleVariable(expr, type))
                }

            // Handle {% ... %} directives
            extractDirectives(text)
                .filter { it !in variables.map { v -> v.name } }
                .forEach { expr ->
                    val type = detectType(expr)
                    variables.add(PebbleVariable(expr, type))
                }
        }

    /**
     * Extracts Pebble/Twig-style {{ ... }} expressions
     * Example: {{ user.name }}, {{ product.price | numberformat }}
     */
    private fun extractExpressions(value: String): List<String> {
        val results = mutableListOf<String>()
        var i = 0

        while (i < value.length) {
            val start = value.indexOf("{{", i)
            if (start == -1) break

            val end = value.indexOf("}}", start + 2)
            if (end == -1) break

            val expr = value.substring(start + 2, end).trim()
            if (expr.isNotEmpty()) results.add(expr)

            i = end + 2
        }

        return results
    }

    /**
     * Extracts Pebble directives like {% for user in users %}, {% if condition %}, {% include "file" %}
     */
    private fun extractDirectives(value: String): List<String> {
        val results = mutableListOf<String>()
        var i = 0

        while (i < value.length) {
            val start = value.indexOf("{%", i)
            if (start == -1) break

            val end = value.indexOf("%}", start + 2)
            if (end == -1) break

            val inner = value.substring(start + 2, end).trim()
            if (inner.isNotEmpty() && !inner.startsWith("end")) {
                // Ignore {% endfor %}, {% endif %}, etc.
                results.add(inner)
            }

            i = end + 2
        }

        return results
    }

    /**
     * Determines variable type based on Pebble syntax.
     */
    private fun detectType(expression: String): VariableType {
        return when {
            expression.startsWith("for ") -> VariableType.COLLECTION
            expression.startsWith("if ") ||
                    expression.startsWith("elseif ") ||
                    expression.startsWith("else") -> VariableType.SINGLE

            expression.startsWith("include ") ||
                    expression.startsWith("import ") ||
                    expression.startsWith("macro ") -> VariableType.OBJECT

            expression.contains(" in ") -> VariableType.COLLECTION
            expression.contains('.') -> VariableType.OBJECT
            else -> VariableType.SINGLE
        }
    }
}
