package pl.ejdev.zwoje.core.template.mustache

import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser
class MustacheVariable(
    name: String,
    type: VariableType
) : TemplateVariable(name, type)

object ZwojeMustacheTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> =
        extract(content) { variables ->
            // Mustache expressions can appear anywhere in the text content or attributes
            val text = this.outerHtml() // safer than text() since it includes all markup
            extractExpressions(text)
                .filter { (_, expr) -> expr !in variables.map { it.name } }
                .forEach { (prefix, expr) ->
                    val type = detectType(prefix, expr)
                    variables.add(MustacheVariable(expr, type))
                }
        }

    /**
     * Extracts Mustache-style expressions from the given text.
     * Supports:
     *   {{var}}, {{{unescaped}}}, {{#section}}, {{/section}}, {{^inverted}}
     *
     * Returns a list of pairs (prefix, content)
     * where prefix is one of "#", "/", "^", "&", "{" or empty for normal variables.
     */
    private fun extractExpressions(value: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        var i = 0

        while (i < value.length) {
            val start = value.indexOf("{{", i)
            if (start == -1) break

            val triple = value.startsWith("{{{", start)
            val end = if (triple)
                value.indexOf("}}}", start + 3)
            else
                value.indexOf("}}", start + 2)

            if (end == -1) break

            val inner = value.substring(
                start + if (triple) 3 else 2,
                end
            ).trim()

            if (inner.isNotEmpty()) {
                val prefix = inner.firstOrNull()?.takeIf { it in listOf('#', '/', '^', '&') }?.toString() ?: ""
                val expr = if (prefix.isNotEmpty()) inner.drop(1).trim() else inner
                results.add(prefix to expr)
            }

            i = end + if (triple) 3 else 2
        }

        return results
    }

    /**
     * Detects variable type based on Mustache syntax.
     */
    private fun detectType(prefix: String, expression: String): VariableType = when (prefix) {
        "#" -> VariableType.COLLECTION  // section start (loop)
        "^" -> VariableType.SINGLE      // inverted section (boolean)
        "/" -> VariableType.SINGLE      // section end, we skip adding anyway
        else -> when {
            expression.contains('.') -> VariableType.OBJECT
            else -> VariableType.SINGLE
        }
    }
}
