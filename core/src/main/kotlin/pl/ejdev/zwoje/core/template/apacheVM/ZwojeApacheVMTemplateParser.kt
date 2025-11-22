package pl.ejdev.zwoje.core.template.apacheVM

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import pl.ejdev.zwoje.core.template.*
import pl.ejdev.zwoje.core.template.thymeleaf.CollectionInfo

private const val TH_EACH = "th:each"
class ApacheVMVariable(
    name: String,
    type: VariableType,
    children: List<TemplateVariable> = emptyList()
) : TemplateVariable(name, type, children) {
        companion object {
        fun collection(info: CollectionInfo): ApacheVMVariable = ApacheVMVariable(
            info.collectionName,
            VariableType.COLLECTION,
            info.itemProperties.map { ApacheVMVariable(it, VariableType.OBJECT) }
        )

        fun collection(collectionVariable: String) = ApacheVMVariable(collectionVariable, VariableType.COLLECTION)
    }
}

object ZwojeApacheVMTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> {
        val doc = Jsoup.parse(content)
        val variables = mutableSetOf<TemplateVariable>()
        val collections = mutableMapOf<String, CollectionInfo>() // iterVar -> CollectionInfo

        // TODO it is just thymeleaf copy - but first resolver problem
        val allElements = doc.allElements.filterNotNull()
        allElements.forEach { element ->
            identifyThEachLoops(element, collections)
        }
        allElements.forEach { element ->
            extractAllVariables(element, variables, collections)
        }
        collections.forEach { (_, info) ->
            addCollectionMetaToVariables(variables, info)
        }

        return variables
    }

    private fun extractAllVariables(
        element: Element,
        variables: MutableSet<TemplateVariable>,
        collections: MutableMap<String, CollectionInfo>
    ) {
        apacheAttrs
            .asSequence()
            .filter { element.attr(it).isNotBlank() }
            .forEach { attributeName ->
                val attrValue = element.attr(attributeName)
                if (attributeName == TH_EACH) {
                    extractThEachVariables(attrValue, variables)
                } else {
                    extractedNonLoopingVariables(attrValue, collections, variables)
                }
            }
    }

    private fun extractedNonLoopingVariables(
        attrValue: String,
        collections: MutableMap<String, CollectionInfo>,
        variables: MutableSet<TemplateVariable>
    ) {
        extractExpressions(attrValue).forEach { (prefix, expression) ->
            val firstPart = isExpressionUsesIteratorVariable(expression)
            if (firstPart in collections) {
                addCollectionVariable(expression, collections, firstPart)
            } else {
                addRegularVariable(variables, expression, prefix)
            }
        }
    }

    private fun isExpressionUsesIteratorVariable(expression: String): String =
        expression.substringBefore(DOT).substringBefore(QUESTION_MARK)

    private fun addCollectionVariable(
        expression: String,
        collections: MutableMap<String, CollectionInfo>,
        firstPart: String
    ) {
        val remainingPath = expression.substringAfter(DOT, "")
        if (remainingPath.isNotEmpty()) {
            collections[firstPart]!!.itemProperties.add(remainingPath)
        }
    }

    private fun addRegularVariable(
        variables: MutableSet<TemplateVariable>,
        expression: String,
        prefix: String
    ) {
        val variablesNames = variables.map { it.name }
        if (expression !in variablesNames) {
            val type = detectType(prefix, expression)
            variables.add(ApacheVMVariable(expression, type))
        }
    }

    private fun extractThEachVariables(
        attrValue: String,
        variables: MutableSet<TemplateVariable>
    ) {
        val (_, collectionVariable) = parseEachExpression(attrValue)
        if (collectionVariable != null) {
            variables.add(ApacheVMVariable.collection(collectionVariable))
        }
    }

    // First pass: identify th:each loops
    private fun identifyThEachLoops(element: Element, collections: MutableMap<String, CollectionInfo>) {
        val eachAttr = element.attr(TH_EACH)
        if (eachAttr.isNotBlank()) {
            val (iterationVariables, collectionVariables) = parseEachExpression(eachAttr)
            if (iterationVariables != null && collectionVariables != null) {
                collections[iterationVariables] = CollectionInfo(collectionVariables)
            }
        }
    }

    // Add collection metadata to variables (for JSON generation)
    private fun addCollectionMetaToVariables(variables: MutableSet<TemplateVariable>, info: CollectionInfo) {
        val collectionVar = variables.find { it.name == info.collectionName }
        if (collectionVar != null) {
            variables.remove(collectionVar)
            variables.add(ApacheVMVariable.collection(info))
        }
    }

    private fun parseEachExpression(eachExpr: String): Pair<String?, String?> {
        val parts = eachExpr.split(":")
        if (parts.size < 2) return null to null

        val iterationVariables = parts[0].trim().substringBefore(",").trim()
        val collectionExpression = parts[1].trim()
        val expressions = extractExpressions(collectionExpression)
        val collectionVariables = expressions.firstOrNull()?.second

        return iterationVariables to collectionVariables
    }

    private fun extractExpressions(value: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        var i = 0
        while (i < value.length) {
            val startIndex = prefixes
                .asSequence()
                .map { prefix -> prefix to value.indexOf(prefix, i) }
                .filter { it.second != -1 }
                .minByOrNull { it.second } ?: break

            val (prefix, start) = startIndex
            val end = value.indexOf(CLOSE_BRACKET, start)
            if (end == -1) break

            val inner = value.substring(start + prefix.length, end).trim()
            results.add(prefix to inner)
            i = end + 1
        }

        return results
    }

    private fun detectType(prefix: String, expression: String): VariableType = when (prefix) {
        "#{", "@{" -> VariableType.SINGLE
        else -> when {
            expression.contains('.') || expression.contains("?.") -> VariableType.OBJECT
            else -> VariableType.SINGLE
        }
    }

    private val prefixes = listOf($$"${", "#{", "@{")
    private val apacheAttrs = listOf(
        "th:text", TH_EACH, "th:if", "th:unless",
        "th:value", "th:with", "th:attr", "th:href", "th:src"
    )
}
