package pl.ejdev.zwoje.core.template.pebble

import pl.ejdev.zwoje.core.template.DOUBLE_CLOSE_BRACKETS
import pl.ejdev.zwoje.core.template.DOUBLE_OPEN_BRACKETS
import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser

private const val FOR = "for "
private const val IN = " in "
private const val IF = "if "
private const val ELSEIF = "elseif "
private const val DOT = "."
private const val END_FOR_START = "end"

class PebbleVariable(
    name: String,
    type: VariableType,
    children: List<TemplateVariable> = emptyList()
) : TemplateVariable(name, type, children)

data class PebbleCollectionInfo(
    val collectionName: String,
    val itemProperties: MutableSet<String> = mutableSetOf()
)

object ZwojePebbleTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> {
        val variables = mutableSetOf<TemplateVariable>()
        val collections = mutableMapOf<String, PebbleCollectionInfo>()
        val loopContexts = mutableMapOf<String, String>() // iterVar -> collectionVar

        val text = content

        // First pass: identify {% for ... in ... %} loops
        extractDirectives(text).forEach { directive ->
            if (directive.startsWith(FOR)) {
                // Parse: {% for item in items %} or {% for item in invoice.items %}
                val parts = directive.removePrefix(FOR).split(IN)
                if (parts.size == 2) {
                    val iterVar = parts[0].trim()
                    val collectionVar = parts[1].trim()
                    loopContexts[iterVar] = collectionVar
                    collections[collectionVar] = PebbleCollectionInfo(collectionVar)
                }
            }
        }

        // Second pass: process all expressions and directives
        extractExpressions(text).forEach { expr ->
            val cleanExpr = cleanExpression(expr)
            if (cleanExpr.isEmpty()) return@forEach

            val firstPart = cleanExpr.split(DOT).first()

            if (firstPart in loopContexts) {
                // This is an item property - add to collection
                val collectionVar = loopContexts[firstPart]!!
                val remainingPath = if (cleanExpr.contains(DOT)) {
                    cleanExpr.substringAfter(DOT)
                } else {
                    // Single variable like {{ item }} - skip it
                    return@forEach
                }

                if (remainingPath.isNotEmpty()) {
                    collections[collectionVar]?.itemProperties?.add(remainingPath)
                }
            } else if (cleanExpr !in collections.keys) {
                // Regular variable
                if (cleanExpr !in variables.map { it.name }) {
                    variables.add(PebbleVariable(cleanExpr, detectType(cleanExpr)))
                }
            }
        }

        // Process directives for additional variables (like {% if condition %})
        extractDirectives(text).forEach { directive ->
            if (directive.startsWith(IF) || directive.startsWith(ELSEIF)) {
                // Extract expression from if/elseif
                val exprPart = directive.removePrefix(IF).removePrefix(ELSEIF).trim()
                val cleanExpr = cleanExpression(exprPart)

                if (cleanExpr.isNotEmpty() && cleanExpr !in variables.map { it.name } && cleanExpr !in collections.keys) {
                    val firstPart = cleanExpr.split(DOT).first()
                    if (firstPart !in loopContexts) {
                        variables.add(PebbleVariable(cleanExpr, detectType(cleanExpr)))
                    }
                }
            }
            // Note: {% for ... %} directives are already processed in first pass
        }

        // Add collections with their children
        collections.forEach { (collectionName, info) ->
            variables.add(PebbleVariable(
                collectionName,
                VariableType.COLLECTION,
                info.itemProperties.map { PebbleVariable(it, VariableType.OBJECT) }
            ))
        }

        return variables
    }

    /**
     * Extracts Pebble/Twig-style {{ ... }} expressions
     * Example: {{ user.name }}, {{ product.price | numberformat }}
     */
    private fun extractExpressions(value: String): List<String> {
        val results = mutableListOf<String>()
        var i = 0

        while (i < value.length) {
            val start = value.indexOf(DOUBLE_OPEN_BRACKETS, i)
            if (start == -1) break

            val end = value.indexOf(DOUBLE_CLOSE_BRACKETS, start + 2)
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
            if (inner.isNotEmpty() && !inner.startsWith(END_FOR_START)) {
                // Ignore {% endfor %}, {% endif %}, etc.
                results.add(inner)
            }

            i = end + 2
        }

        return results
    }

    /**
     * Cleans expression by removing filters and operators
     * Example: "user.name | upper" -> "user.name"
     * Example: "items | length" -> "items"
     */
    private fun cleanExpression(expr: String): String {
        // Remove everything after pipe (filters)
        var cleaned = expr.split("|")[0].trim()

        // Remove comparison operators and their right-hand side
        operators.forEach {
            if (cleaned.contains(it)) {
                cleaned = cleaned.substringBefore(it).trim()
            }
        }

        // Remove method calls
        cleaned = cleaned.split("(")[0].trim()

        // Remove quotes for string literals
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
            (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            return ""
        }

        // Remove numeric literals
        if (cleaned.toIntOrNull() != null || cleaned.toDoubleOrNull() != null) {
            return ""
        }

        return cleaned
    }

    /**
     * Determines variable type based on Pebble syntax.
     */
    private fun detectType(expression: String): VariableType = when {
        expression.contains('.') -> VariableType.OBJECT
        else -> VariableType.SINGLE
    }

    private val operators = listOf(" == ", " != ", " > ", " < ", " >= ", " <= ", " and ", " or ")
}