package pl.ejdev.zwoje.core.template.groovyTemplates

import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser

class GroovyMarkupVariable(
    name: String,
    type: VariableType
) : TemplateVariable(name, type)

object ZwojeGroovyMarkupTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> {
        val variables = mutableSetOf<TemplateVariable>()

        // Extract Groovy string interpolations like $var or ${expr}
        extractInterpolations(content)
            .filter { it !in variables.map { v -> v.name } }
            .forEach { expr ->
                val type = detectType(expr)
                variables.add(GroovyMarkupVariable(expr, type))
            }

        // Extract Groovy control flow (if / for)
        extractControlStructures(content)
            .filter { it !in variables.map { v -> v.name } }
            .forEach { expr ->
                val type = detectType(expr)
                variables.add(GroovyMarkupVariable(expr, type))
            }

        return variables
    }


    /**
     * Extracts Groovy string interpolations: $var or ${expression}
     */
    private fun extractInterpolations(value: String): List<String> {
        val results = mutableListOf<String>()
        var i = 0
        while (i < value.length) {
            val dollar = value.indexOf('$', i)
            if (dollar == -1 || dollar == value.lastIndex) break

            when {
                // ${...} syntax
                value[dollar + 1] == '{' -> {
                    val end = value.indexOf('}', dollar + 2)
                    if (end == -1) break
                    val expr = value.substring(dollar + 2, end).trim()
                    if (expr.isNotEmpty()) results.add(expr)
                    i = end + 1
                }

                // $identifier syntax
                value[dollar + 1].isJavaIdentifierStart() -> {
                    var j = dollar + 2
                    while (j < value.length && value[j].isJavaIdentifierPart()) j++
                    val varName = value.substring(dollar + 1, j)
                    results.add(varName)
                    i = j
                }

                // Escaped dollar sign
                value[dollar - 1] == '\\' -> i = dollar + 1

                else -> i = dollar + 1
            }
        }
        return results
    }

    /**
     * Extracts Groovy control structures like:
     *   if (amount > 100)
     *   for (item in items)
     */
    private fun extractControlStructures(value: String): List<String> {
        val results = mutableListOf<String>()
        val lines = value.lines()
        lines.map { it.trim() }.forEach { line ->
            when {
                line.startsWith("if ") || line.startsWith("if(") -> {
                    val expr = line.substringAfter("if").trim()
                    results.add(expr)
                }

                line.startsWith("for ") || line.startsWith("for(") -> {
                    val expr = line.substringAfter("for").trim()
                    results.add(expr)
                }
            }
        }
        return results
    }

    private fun detectType(expression: String): VariableType = when {
        expression.contains(" in ") -> VariableType.COLLECTION
        expression.startsWith("for ") -> VariableType.COLLECTION
        expression.startsWith("if ") -> VariableType.SINGLE
        expression.contains('.') -> VariableType.OBJECT
        else -> VariableType.SINGLE
    }
}
