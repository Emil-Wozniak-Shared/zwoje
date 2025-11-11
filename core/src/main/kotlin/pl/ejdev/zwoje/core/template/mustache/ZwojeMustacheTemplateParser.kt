package pl.ejdev.zwoje.core.template.mustache

import pl.ejdev.zwoje.core.template.CARET
import pl.ejdev.zwoje.core.template.DOUBLE_CLOSE_BRACKETS
import pl.ejdev.zwoje.core.template.DOUBLE_OPEN_BRACKETS
import pl.ejdev.zwoje.core.template.HASH
import pl.ejdev.zwoje.core.template.SLASH
import pl.ejdev.zwoje.core.template.TRIPLE_CLOSE_BRACKETS
import pl.ejdev.zwoje.core.template.TRIPLE_OPEN_BRACKETS
import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser

class MustacheVariable(
    name: String,
    type: VariableType,
    children: List<TemplateVariable> = emptyList()
) : TemplateVariable(name, type, children) {
    companion object {
        fun collection(collectionName: String, info: MustacheCollectionInfo) = MustacheVariable(
            collectionName,
            VariableType.COLLECTION,
            info.itemProperties.map { MustacheVariable(it, VariableType.OBJECT) }
        )
    }
}

data class MustacheCollectionInfo(
    val collectionName: String,
    val itemProperties: MutableSet<String> = mutableSetOf()
)

object ZwojeMustacheTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> {
        val variables = mutableSetOf<TemplateVariable>()
        val collections = mutableMapOf<String, MustacheCollectionInfo>()
        val activeCollections = mutableListOf<String>() // Stack to track nested sections

        extractExpressions(content).forEach { (prefix, expression) ->
            when (prefix) {
                HASH -> addCollection(activeCollections, expression, collections)
                SLASH -> sectionEndPopFromStack(activeCollections, expression)
                CARET -> invertedSection(expression, variables, collections)
                else -> regularVariableOrPropertyAccess(activeCollections, collections, expression, variables)
            }
        }

        collections.forEach { (collectionName, info) ->
            variables.add(MustacheVariable.collection(collectionName, info))
        }

        return variables
    }

    private fun invertedSection(
        expression: String,
        variables: MutableSet<TemplateVariable>,
        collections: MutableMap<String, MustacheCollectionInfo>
    ) {
        if (expression !in variables.map { it.name } && expression !in collections.keys) {
            variables.add(MustacheVariable(expression, VariableType.SINGLE))
        }
    }

    private fun regularVariableOrPropertyAccess(
        activeCollections: MutableList<String>,
        collections: MutableMap<String, MustacheCollectionInfo>,
        expression: String,
        variables: MutableSet<TemplateVariable>
    ) {
        if (activeCollections.isNotEmpty()) {
            val currentCollection = activeCollections.last()

            // In Mustache, inside {{#invoice.items}}, {{name}} refers to item.name
            // We need to add this as a property to the collection
            collections[currentCollection]?.itemProperties?.add(expression)
        } else {
            // Regular variable outside any collection
            if (expression !in variables.map { it.name } && expression !in collections.keys) {
                val type = detectType("", expression)
                variables.add(MustacheVariable(expression, type))
            }
        }
    }

    private fun sectionEndPopFromStack(activeCollections: MutableList<String>, expression: String) {
        if (activeCollections.isNotEmpty() && activeCollections.last() == expression) {
            activeCollections.removeAt(activeCollections.lastIndex)
        }
    }

    private fun addCollection(
        activeCollections: MutableList<String>,
        expression: String,
        collections: MutableMap<String, MustacheCollectionInfo>
    ) {
        activeCollections.add(expression)
        collections[expression] = MustacheCollectionInfo(expression)
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
            val start = value.indexOf(DOUBLE_OPEN_BRACKETS, i)
            if (start == -1) break

            val triple = value.startsWith(TRIPLE_OPEN_BRACKETS, start)
            val end = if (triple)
                value.indexOf(TRIPLE_CLOSE_BRACKETS, start + 3)
            else
                value.indexOf(DOUBLE_CLOSE_BRACKETS, start + 2)

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
        HASH -> VariableType.COLLECTION  // section start (loop)
        CARET -> VariableType.SINGLE      // inverted section (boolean)
        SLASH -> VariableType.SINGLE      // section end, we skip adding anyway
        else -> when {
            expression.contains('.') -> VariableType.OBJECT
            else -> VariableType.SINGLE
        }
    }
}