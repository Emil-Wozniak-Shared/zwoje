package pl.ejdev.zwoje.core.template.thymeleaf

import org.jsoup.Jsoup
import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser

class ThymeleafVariable(
    name: String,
    type: VariableType,
    children: List<TemplateVariable> = emptyList()
) : TemplateVariable(name, type, children)

data class CollectionInfo(
    val collectionName: String,
    val itemProperties: MutableSet<String> = mutableSetOf()
)

object ZwojeThymeleafTemplateParser : ZwojeTemplateParser() {
    override fun parse(content: String): Set<TemplateVariable> {
        val doc = Jsoup.parse(content)
        val variables = mutableSetOf<TemplateVariable>()
        val collections = mutableMapOf<String, CollectionInfo>() // iterVar -> CollectionInfo

        // First pass: identify th:each loops
        doc.allElements.forEach { element ->
            val eachAttr = element.attr("th:each")
            if (eachAttr.isNotBlank()) {
                val (iterVar, collectionVar) = parseEachExpression(eachAttr)
                if (iterVar != null && collectionVar != null) {
                    collections[iterVar] = CollectionInfo(collectionVar)
                }
            }
        }

        // Second pass: extract all variables
        doc.allElements.forEach { element ->
            thymeleafAttrs
                .filter { element.attr(it).isNotBlank() }
                .forEach { attrName ->
                    val attrValue = element.attr(attrName)

                    if (attrName == "th:each") {
                        val (iterVar, collectionVar) = parseEachExpression(attrValue)
                        if (collectionVar != null) {
                            variables.add(ThymeleafVariable(collectionVar, VariableType.COLLECTION))
                        }
                    } else {
                        extractExpressions(attrValue).forEach { (prefix, expr) ->
                            // Check if this expression uses an iterator variable
                            val firstPart = expr.split(".")[0].split("?")[0]

                            if (firstPart in collections) {
                                // This is an item property, add it to the collection's properties
                                val remainingPath = expr.substringAfter(".", "")
                                if (remainingPath.isNotEmpty()) {
                                    collections[firstPart]!!.itemProperties.add(remainingPath)
                                }
                            } else {
                                // Regular variable
                                if (expr !in variables.map { it.name }) {
                                    val type = detectType(prefix, expr)
                                    variables.add(ThymeleafVariable(expr, type))
                                }
                            }
                        }
                    }
                }
        }

        // Add collection metadata to variables (for JSON generation)
        collections.forEach { (_, info) ->
            val collectionVar = variables.find { it.name == info.collectionName }
            if (collectionVar != null) {
                variables.remove(collectionVar)
                variables.add(ThymeleafVariable(
                    info.collectionName,
                    VariableType.COLLECTION,
                    info.itemProperties.map { ThymeleafVariable(it, VariableType.OBJECT) }
                ))
            }
        }

        return variables
    }

    private fun parseEachExpression(eachExpr: String): Pair<String?, String?> {
        val parts = eachExpr.split(":")
        if (parts.size < 2) return null to null

        val iterVar = parts[0].trim().split(",")[0].trim()
        val collectionExpr = parts[1].trim()

        val expressions = extractExpressions(collectionExpr)
        val collectionVar = expressions.firstOrNull()?.second

        return iterVar to collectionVar
    }

    private fun extractExpressions(value: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        val prefixes = listOf("\${", "#{", "@{")
        var i = 0

        while (i < value.length) {
            val startIndex = prefixes
                .map { prefix -> prefix to value.indexOf(prefix, i) }
                .filter { it.second != -1 }
                .minByOrNull { it.second } ?: break

            val (prefix, start) = startIndex
            val end = value.indexOf('}', start)
            if (end == -1) break

            val inner = value.substring(start + prefix.length, end).trim()
            results.add(prefix to inner)
            i = end + 1
        }

        return results
    }

    private fun detectType(prefix: String, expression: String): VariableType {
        return when (prefix) {
            "#{", "@{" -> VariableType.SINGLE
            else -> when {
                expression.contains('.') || expression.contains("?.") -> VariableType.OBJECT
                else -> VariableType.SINGLE
            }
        }
    }

    private val thymeleafAttrs = listOf(
        "th:text", "th:each", "th:if", "th:unless",
        "th:value", "th:with", "th:attr", "th:href", "th:src"
    )
}
