package pl.ejdev.zwoje.core.template.freemarker

import pl.ejdev.zwoje.core.template.*

private const val AS = " as "
private const val LIST = "list "
private const val LIST_START = "list_start"
private const val LIST_END = "list_end"
private const val DOLLAR = "dollar"
private const val OPENING_DOLLAR_SIGN_BRACKET = $$"${"

class FreemarkerVariable(
    name: String,
    type: VariableType,
    children: List<TemplateVariable> = emptyList()
) : TemplateVariable(name, type, children) {

    companion object {
        fun collection(collectionName: String, info: FreemarkerCollectionInfo) = FreemarkerVariable(
            collectionName,
            VariableType.COLLECTION,
            info.itemProperties.map { FreemarkerVariable(it, VariableType.OBJECT) }
        )
    }
}

data class FreemarkerCollectionInfo(
    val collectionName: String,
    val itemProperties: MutableSet<String> = mutableSetOf()
)

object ZwojeFreemarkerTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> {
        val variables = mutableSetOf<TemplateVariable>()
        val collections = mutableMapOf<String, FreemarkerCollectionInfo>()
        val activeCollections = mutableListOf<Pair<String, String>>()
        extractDirectivesIdentifyCollections(content, activeCollections, collections)
        extractDollarExpressionsWithContextAwareness(content)
        processExpressions(activeCollections, content, variables, collections)
        reprocessWithProperContextTracking(content, collections)

        val listContexts = mutableMapOf<String, String>()

        extractDirectives(content).forEach { directive ->
            if (directive.startsWith(LIST)) {
                val parts = directive.removePrefix(LIST).split(AS)
                if (parts.size == 2) {
                    val collectionVar = parts[0].trim()
                    val iterVar = parts[1].trim()
                    listContexts[iterVar] = collectionVar
                }
            }
        }

        processAllDollarExpressions(content, listContexts, collections, variables)

        // Add collections with their children
        collections.forEach { (collectionName, info) ->
            variables.add(FreemarkerVariable.collection(collectionName, info))
        }

        return variables
    }

    private fun processAllDollarExpressions(
        text: String,
        listContexts: MutableMap<String, String>,
        collections: MutableMap<String, FreemarkerCollectionInfo>,
        variables: MutableSet<TemplateVariable>
    ) {
        extractDollarExpressions(text).forEach { expr ->
            val cleanExpr = cleanExpression(expr)
            if (cleanExpr.isEmpty()) return@forEach

            val firstPart = cleanExpr.split(DOT).first().split(QUESTION_MARK).first()

            if (firstPart in listContexts) {
                // This is an item property - add to collection
                val collectionVariable = listContexts[firstPart]!!
                val remainingPath = getRemainingPath(cleanExpr, firstPart).removePrefix(DOT)

                if (remainingPath.isNotEmpty()) {
                    collections[collectionVariable]?.itemProperties?.add(remainingPath)
                }
            } else if (cleanExpr !in collections.keys) {
                // Regular variable
                if (cleanExpr !in variables.map { it.name }) {
                    variables.add(FreemarkerVariable(cleanExpr, detectType(cleanExpr)))
                }
            }
        }
    }

    private fun getRemainingPath(cleanExpr: String, firstPart: String): String =
        if (cleanExpr.contains(DOT)) cleanExpr.substringAfter(DOT)
        else cleanExpr.substringAfter(firstPart)

    private fun extractDollarExpressionsWithContextAwareness(text: String) {
        val allExpressions = mutableListOf<Pair<String, Boolean>>() // (expression, insideCollection)
        var currentIndex = 0
        var insideListDepth = 0

        while (currentIndex < text.length) {
            val listStart = text.indexOf("<#list ", currentIndex)
            val listEnd = text.indexOf("</#list>", currentIndex)
            val dollarStart = text.indexOf(OPENING_DOLLAR_SIGN_BRACKET, currentIndex)

            // Determine what comes next
            val nextEvent = listOf(
                Triple(LIST_START, listStart, listStart != -1),
                Triple(LIST_END, listEnd, listEnd != -1),
                Triple(DOLLAR, dollarStart, dollarStart != -1)
            ).filter { it.third }.minByOrNull { it.second }

            when (nextEvent?.first) {
                LIST_START -> {
                    insideListDepth++
                    currentIndex = listStart + 7
                }

                LIST_END -> {
                    insideListDepth--
                    currentIndex = listEnd + 8
                }

                DOLLAR -> {
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
    }

    private fun extractDirectivesIdentifyCollections(
        text: String,
        activeCollections: MutableList<Pair<String, String>>,
        collections: MutableMap<String, FreemarkerCollectionInfo>
    ) {
        extractDirectives(text).forEach { directive ->
            if (directive.startsWith(LIST)) {
                // Parse: <#list users as user> or <#list invoice.items as item>
                val parts = directive.removePrefix(LIST).split(AS)
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
    }

    private fun processExpressions(
        activeCollections: MutableList<Pair<String, String>>,
        text: String,
        variables: MutableSet<TemplateVariable>,
        collections: MutableMap<String, FreemarkerCollectionInfo>
    ) {
        activeCollections.clear()
        extractDirectives(text).forEach { directive ->
            if (directive.startsWith(LIST)) {
                val parts = directive.removePrefix(LIST).split(AS)
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
    }

    private fun reprocessWithProperContextTracking(
        text: String,
        collections: MutableMap<String, FreemarkerCollectionInfo>
    ) {
        extractDollarExpressions(text).forEach { expr ->
            val cleanExpr = cleanExpression(expr)
            if (cleanExpr.isEmpty()) return@forEach

            // Check if this expression uses an iterator variable
            val firstPart = cleanExpr.substringBefore(DOT).substringBefore(QUESTION_MARK)
            val matchingCollection = collections.keys.find { collectionKey ->
                val parts = collectionKey.split(AS)
                parts.size == 2 && parts[1].trim() == firstPart
            }

            if (matchingCollection != null) {
                // This is an item property
                val collectionName = matchingCollection.split(AS)[0].trim()
                val remainingPath = cleanExpr.substringAfter(DOT, "")
                if (remainingPath.isNotEmpty()) {
                    collections[collectionName]?.itemProperties?.add(remainingPath)
                }
            }
        }
    }

    private fun extractDollarExpressions(value: String): List<String> {
        val results = mutableListOf<String>()
        var i = 0

        while (i < value.length) {
            val start = value.indexOf(OPENING_DOLLAR_SIGN_BRACKET, i)
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
        var cleaned = expr.substringBefore(QUESTION_MARK).trim()
        // Remove method calls like someMethod()
        cleaned = cleaned.substringBefore("(").trim()
        return cleaned
    }

    private fun detectType(expression: String): VariableType = when {
        expression.contains('.') -> VariableType.OBJECT
        else -> VariableType.SINGLE
    }
}