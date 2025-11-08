package pl.ejdev.zwoje.core.template.mustache

import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser

class MustacheVariable(
    name: String,
    type: VariableType,
    children: List<TemplateVariable> = emptyList()
) : TemplateVariable(name, type, children)

data class MustacheCollectionInfo(
    val collectionName: String,
    val itemProperties: MutableSet<String> = mutableSetOf()
)

object ZwojeMustacheTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> {
        val variables = mutableSetOf<TemplateVariable>()
        val collections = mutableMapOf<String, MustacheCollectionInfo>()
        val activeCollections = mutableListOf<String>() // Stack to track nested sections

        val expressions = extractExpressions(content)

        expressions.forEach { (prefix, expr) ->
            when (prefix) {
                "#" -> {
                    // Section start - this is a collection
                    activeCollections.add(expr)
                    collections[expr] = MustacheCollectionInfo(expr)
                }
                "/" -> {
                    // Section end - pop from stack
                    if (activeCollections.isNotEmpty() && activeCollections.last() == expr) {
                        activeCollections.removeAt(activeCollections.lastIndex)
                    }
                }
                "^" -> {
                    // Inverted section (boolean check)
                    if (expr !in variables.map { it.name } && expr !in collections.keys) {
                        variables.add(MustacheVariable(expr, VariableType.SINGLE))
                    }
                }
                else ->
                    // Regular variable or property access
                    if (activeCollections.isNotEmpty()) {
                        // We're inside a collection section
                        val currentCollection = activeCollections.last()

                        // In Mustache, inside {{#invoice.items}}, {{name}} refers to item.name
                        // We need to add this as a property to the collection
                        collections[currentCollection]?.itemProperties?.add(expr)
                    } else {
                        // Regular variable outside any collection
                        if (expr !in variables.map { it.name } && expr !in collections.keys) {
                            val type = detectType("", expr)
                            variables.add(MustacheVariable(expr, type))
                        }
                    }
            }
        }

        // Add collection variables with their item properties
        collections.forEach { (collectionName, info) ->
            variables.add(MustacheVariable(
                collectionName,
                VariableType.COLLECTION,
                info.itemProperties.map { MustacheVariable(it, VariableType.OBJECT) }
            ))
        }

        return variables
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