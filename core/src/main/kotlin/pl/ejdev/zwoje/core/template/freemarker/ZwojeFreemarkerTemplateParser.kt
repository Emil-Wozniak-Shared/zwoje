package pl.ejdev.zwoje.core.template.freemarker

import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser

class FreemarkerVariable(
    name: String,
    type: VariableType,
    children: List<TemplateVariable> = emptyList()
) : TemplateVariable(name, type, children)

data class FreemarkerCollectionInfo(
    val collectionName: String,
    val itemProperties: MutableSet<String> = mutableSetOf()
)

object ZwojeFreemarkerTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> {
        val variables = mutableSetOf<TemplateVariable>()
        val collections = mutableMapOf<String, FreemarkerCollectionInfo>()
        val activeCollections = mutableListOf<Pair<String, String>>() // Stack: (iterVar, collectionVar)

        val text = content

        // First pass: extract directives to identify collections
        extractDirectives(text).forEach { directive ->
            if (directive.startsWith("list ")) {
                // Parse: <#list users as user> or <#list invoice.items as item>
                val parts = directive.removePrefix("list ").split(" as ")
                if (parts.size == 2) {
                    val collectionVar = parts[0].trim()
                    val iterVar = parts[1].trim()
                    activeCollections.add(iterVar to collectionVar)
                    collections[collectionVar] = FreemarkerCollectionInfo(collectionVar)
                }
            } else if (directive.startsWith("/list")) {
                // Pop from stack when exiting list
                if (activeCollections.isNotEmpty()) {
                    activeCollections.removeAt(activeCollections.lastIndex)
                }
            }
        }

        // Second pass: extract ${...} expressions with context awareness
        val allExpressions = mutableListOf<Pair<String, Boolean>>() // (expression, insideCollection)
        var currentIndex = 0
        var insideListDepth = 0

        while (currentIndex < text.length) {
            val listStart = text.indexOf("<#list ", currentIndex)
            val listEnd = text.indexOf("</#list>", currentIndex)
            val dollarStart = text.indexOf("\${", currentIndex)

            // Determine what comes next
            val nextEvent = listOf(
                Triple("list_start", listStart, listStart != -1),
                Triple("list_end", listEnd, listEnd != -1),
                Triple("dollar", dollarStart, dollarStart != -1)
            ).filter { it.third }.minByOrNull { it.second }

            when (nextEvent?.first) {
                "list_start" -> {
                    insideListDepth++
                    currentIndex = listStart + 7
                }
                "list_end" -> {
                    insideListDepth--
                    currentIndex = listEnd + 8
                }
                "dollar" -> {
                    val end = text.indexOf('}', dollarStart + 2)
                    if (end != -1) {
                        val expr = text.substring(dollarStart + 2, end).trim()
                        allExpressions.add(expr to (insideListDepth > 0))
                        currentIndex = end + 1
                    } else {
                        break
                    }
                }
                else -> break
            }
        }

        // Process expressions
        activeCollections.clear()
        extractDirectives(text).forEach { directive ->
            if (directive.startsWith("list ")) {
                val parts = directive.removePrefix("list ").split(" as ")
                if (parts.size == 2) {
                    val collectionVar = parts[0].trim()
                    val iterVar = parts[1].trim()
                    activeCollections.add(iterVar to collectionVar)
                }
            } else if (directive.startsWith("/list")) {
                if (activeCollections.isNotEmpty()) {
                    activeCollections.removeAt(activeCollections.lastIndex)
                }
            } else {
                // Process other expressions from directives (like <#if ${condition}>)
                val dollarExprs = extractDollarExpressions(directive)
                dollarExprs.forEach { expr ->
                    val cleanExpr = cleanExpression(expr)
                    if (cleanExpr.isNotEmpty() && cleanExpr !in variables.map { it.name } && cleanExpr !in collections.keys) {
                        variables.add(FreemarkerVariable(cleanExpr, detectType(cleanExpr)))
                    }
                }
            }
        }

        // Process dollar expressions with collection context
        var listDepth = 0
        var currentIterVar: String? = null
        var currentCollectionVar: String? = null

        extractDirectives(text).forEach { directive ->
            if (directive.startsWith("list ")) {
                val parts = directive.removePrefix("list ").split(" as ")
                if (parts.size == 2) {
                    currentCollectionVar = parts[0].trim()
                    currentIterVar = parts[1].trim()
                    listDepth++
                }
            } else if (directive.startsWith("/list")) {
                listDepth--
                if (listDepth == 0) {
                    currentIterVar = null
                    currentCollectionVar = null
                }
            }
        }

        // Re-process with proper context tracking
        extractDollarExpressions(text).forEach { expr ->
            val cleanExpr = cleanExpression(expr)
            if (cleanExpr.isEmpty()) return@forEach

            // Check if this expression uses an iterator variable
            val firstPart = cleanExpr.split(".").first().split("?").first()
            val matchingCollection = collections.keys.find { collectionKey ->
                val parts = collectionKey.split(" as ")
                parts.size == 2 && parts[1].trim() == firstPart
            }

            if (matchingCollection != null) {
                // This is an item property
                val collectionName = matchingCollection.split(" as ")[0].trim()
                val remainingPath = cleanExpr.substringAfter(".", "")
                if (remainingPath.isNotEmpty()) {
                    collections[collectionName]?.itemProperties?.add(remainingPath)
                }
            }
        }

        // Simplified approach: parse more carefully
        val listContexts = mutableMapOf<String, String>() // iterVar -> collectionVar

        extractDirectives(text).forEach { directive ->
            if (directive.startsWith("list ")) {
                val parts = directive.removePrefix("list ").split(" as ")
                if (parts.size == 2) {
                    val collectionVar = parts[0].trim()
                    val iterVar = parts[1].trim()
                    listContexts[iterVar] = collectionVar
                }
            }
        }

        // Process all dollar expressions
        extractDollarExpressions(text).forEach { expr ->
            val cleanExpr = cleanExpression(expr)
            if (cleanExpr.isEmpty()) return@forEach

            val firstPart = cleanExpr.split(".").first().split("?").first()

            if (firstPart in listContexts) {
                // This is an item property - add to collection
                val collectionVar = listContexts[firstPart]!!
                val remainingPath = if (cleanExpr.contains(".")) {
                    cleanExpr.substringAfter(".")
                } else {
                    cleanExpr.substringAfter(firstPart)
                }.removePrefix(".")

                if (remainingPath.isNotEmpty()) {
                    collections[collectionVar]?.itemProperties?.add(remainingPath)
                }
            } else if (cleanExpr !in collections.keys) {
                // Regular variable
                if (cleanExpr !in variables.map { it.name }) {
                    variables.add(FreemarkerVariable(cleanExpr, detectType(cleanExpr)))
                }
            }
        }

        // Add collections with their children
        collections.forEach { (collectionName, info) ->
            variables.add(FreemarkerVariable(
                collectionName,
                VariableType.COLLECTION,
                info.itemProperties.map { FreemarkerVariable(it, VariableType.OBJECT) }
            ))
        }

        return variables
    }

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

    private fun extractDirectives(value: String): List<String> {
        val results = mutableListOf<String>()
        var i = 0

        while (i < value.length) {
            val start = value.indexOf("<#", i)
            if (start == -1) break

            val end = value.indexOf(">", start + 2)
            if (end == -1) break

            val inner = value.substring(start + 2, end).trim()
            if (inner.isNotEmpty()) {
                results.add(inner)
            }

            i = end + 1
        }

        return results
    }

    private fun cleanExpression(expr: String): String {
        // Remove FreeMarker built-ins like ?size, ?exists, etc.
        var cleaned = expr.split("?")[0].trim()
        // Remove method calls like someMethod()
        cleaned = cleaned.split("(")[0].trim()
        return cleaned
    }

    private fun detectType(expression: String): VariableType = when {
        expression.contains('.') -> VariableType.OBJECT
        else -> VariableType.SINGLE
    }
}