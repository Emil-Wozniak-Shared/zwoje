package pl.ejdev.zwoje.core.template.freemarker

import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser

class FreemarkerVariable(
    name: String,
    type: VariableType
) : TemplateVariable(name, type)

object ZwojeFreemarkerTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> =
        extract(content) { variables ->
            val text = this.outerHtml()

            // Extract ${...} expressions
            extractDollarExpressions(text)
                .filter { it !in variables.map { v -> v.name } }
                .forEach { expr ->
                    val type = detectType(expr)
                    variables.add(FreemarkerVariable(expr, type))
                }

            // Extract <#...> directives
            extractDirectives(text)
                .filter { it !in variables.map { v -> v.name } }
                .forEach { expr ->
                    val type = detectType(expr)
                    variables.add(FreemarkerVariable(expr, type))
                }
        }

    /**
     * Extracts ${...} expressions like ${user.name} or ${users?size}
     */
    private fun extractDollarExpressions(value: String): List<String> {
        val results = mutableListOf<String>()
        var i = 0

        while (i < value.length) {
            val start = value.indexOf("\${", i)
            if (start == -1) break

            val end = value.indexOf('}', start + 2)
            if (end == -1) break

            val expr = value.substring(start + 2, end).trim()
            if (expr.isNotEmpty()) results.add(expr)

            i = end + 1
        }

        return results
    }

    /**
     * Extracts FreeMarker directives like <#list users as user>, <#if condition>, <#include "file.ftl">
     */
    private fun extractDirectives(value: String): List<String> {
        val results = mutableListOf<String>()
        var i = 0

        while (i < value.length) {
            val start = value.indexOf("<#", i)
            if (start == -1) break

            val end = value.indexOf(">", start + 2)
            if (end == -1) break

            val inner = value.substring(start + 2, end).trim()
            if (inner.isNotEmpty() && !inner.startsWith("/")) {
                // ignore closing tags like </#list>
                results.add(inner)
            }

            i = end + 1
        }

        return results
    }

    /**
     * Detects variable type based on expression or directive content.
     */
    private fun detectType(expression: String): VariableType {
        return when {
            expression.startsWith("list ") -> VariableType.COLLECTION
            expression.startsWith("if ") ||
                    expression.startsWith("elseif ") ||
                    expression.startsWith("else") -> VariableType.SINGLE
            expression.startsWith("assign ") -> VariableType.OBJECT
            expression.startsWith("import ") ||
                    expression.startsWith("include ") -> VariableType.SINGLE
            expression.contains('.') -> VariableType.OBJECT
            else -> VariableType.SINGLE
        }
    }
}
