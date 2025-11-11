package pl.ejdev.zwoje.core.template.groovyTemplates

import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser
import java.util.regex.Pattern

class GroovyMarkupVariable(
    name: String,
    type: VariableType,
    children: List<TemplateVariable> = emptyList()
) : TemplateVariable(name, type, children)

object ZwojeGroovyMarkupTemplateParser : ZwojeTemplateParser() {

    // Regex patterns for different variable references
    private val directVariablePattern = Pattern.compile("""(?:yield|yieldUnescaped)\s*\(?['"].*?\$\{(\w+(?:\.\w+)*)\}.*?['"]""")
    private val tagContentPattern = Pattern.compile("""(\w+)\s*\(([^)]+)\)""")
    private val eachPattern = Pattern.compile("""(\w+(?:\.\w+)*)\s*\.\s*each\s*\{\s*(\w+)\s*->""")
    private val propertyAccessPattern = Pattern.compile("""(\w+)\.(\w+)""")

    override fun parse(content: String): Set<TemplateVariable> {
        val variables = mutableMapOf<String, GroovyMarkupVariable>()

        // Remove comments
        val cleanContent = content.lines()
            .filter { !it.trim().startsWith("//") }
            .joinToString("\n")

        // Extract variables from .each {} blocks (collections)
        extractCollectionVariables(cleanContent, variables)

        // Extract simple variable references
        extractSimpleVariables(cleanContent, variables)

        // Extract variables from tag content
        extractTagContentVariables(cleanContent, variables)

        // Extract variables from GString interpolations
        extractGStringVariables(cleanContent, variables)

        return variables.values.toSet()
    }

    private fun extractCollectionVariables(
        content: String,
        variables: MutableMap<String, GroovyMarkupVariable>
    ) {
        val matcher = eachPattern.matcher(content)

        while (matcher.find()) {
            val fullPath = matcher.group(1)
            val itemVar = matcher.group(2)

            // Find the block content for this .each
            val blockStart = matcher.end()
            val blockContent = extractBlock(content, blockStart)

            // Extract properties accessed on the item variable
            val children = extractItemProperties(blockContent, itemVar)

            // Register the collection variable
            val parts = fullPath.split(".")
            if (parts.size > 1) {
                // e.g., "invoice.items"
                val objectName = parts[0]
                val collectionName = parts.drop(1).joinToString(".")

                // Register parent object if not exists
                if (!variables.containsKey(objectName)) {
                    variables[objectName] = GroovyMarkupVariable(objectName, VariableType.OBJECT)
                }

                // Register collection with children
                val fullName = fullPath
                variables[fullName] = GroovyMarkupVariable(
                    fullName,
                    VariableType.COLLECTION,
                    children
                )
            } else {
                variables[fullPath] = GroovyMarkupVariable(
                    fullPath,
                    VariableType.COLLECTION,
                    children
                )
            }
        }
    }

    private fun extractSimpleVariables(
        content: String,
        variables: MutableMap<String, GroovyMarkupVariable>
    ) {
        // Match standalone variable names (not already part of property access)
        val simpleVarPattern = Pattern.compile("""(?:span|li|td|div|p|h\d)\s*\((\w+)\)""")
        val matcher = simpleVarPattern.matcher(content)

        while (matcher.find()) {
            val varName = matcher.group(1)

            // Skip if it's a string literal or number
            if (varName.matches(Regex("""['"\d].*"""))) continue

            // Check if it's not already registered as part of an object/collection
            if (!variables.containsKey(varName) && !isPartOfExistingVariable(varName, variables)) {
                variables[varName] = GroovyMarkupVariable(varName, VariableType.SINGLE)
            }
        }
    }

    private fun extractTagContentVariables(
        content: String,
        variables: MutableMap<String, GroovyMarkupVariable>
    ) {
        val matcher = tagContentPattern.matcher(content)

        while (matcher.find()) {
            val varRef = matcher.group(2).trim()

            // Skip string literals, numbers, and closures
            if (varRef.startsWith("'") || varRef.startsWith("\"") ||
                varRef.matches(Regex("""\d+.*""")) || varRef.contains("{")) {
                continue
            }

            // Check for property access (e.g., item.name)
            val propMatcher = propertyAccessPattern.matcher(varRef)
            if (propMatcher.matches()) {
                val objectName = propMatcher.group(1)
                // This is handled by extractItemProperties or should be object property
                continue
            }

            // Simple variable reference
            if (!variables.containsKey(varRef) && !isPartOfExistingVariable(varRef, variables)) {
                variables[varRef] = GroovyMarkupVariable(varRef, VariableType.SINGLE)
            }
        }
    }

    private fun extractGStringVariables(
        content: String,
        variables: MutableMap<String, GroovyMarkupVariable>
    ) {
        val gstringPattern = Pattern.compile("""\$\{(\w+(?:\.\w+)*)\}""")
        val matcher = gstringPattern.matcher(content)

        while (matcher.find()) {
            val varPath = matcher.group(1)
            val parts = varPath.split(".")

            if (parts.size == 1) {
                // Simple variable
                if (!variables.containsKey(varPath)) {
                    variables[varPath] = GroovyMarkupVariable(varPath, VariableType.SINGLE)
                }
            }
        }
    }

    private fun extractItemProperties(blockContent: String, itemVar: String): List<TemplateVariable> {
        val properties = mutableSetOf<String>()
        val pattern = Pattern.compile("""$itemVar\.(\w+)""")
        val matcher = pattern.matcher(blockContent)

        while (matcher.find()) {
            properties.add(matcher.group(1))
        }

        return properties.map { GroovyMarkupVariable(it, VariableType.SINGLE) }
    }

    private fun extractBlock(content: String, startPos: Int): String {
        var braceCount = 0
        var inBlock = false
        val result = StringBuilder()

        for (i in startPos until content.length) {
            val char = content[i]

            if (char == '{') {
                braceCount++
                inBlock = true
            } else if (char == '}') {
                braceCount--
                if (braceCount == 0) break
            }

            if (inBlock) {
                result.append(char)
            }
        }

        return result.toString()
    }

    private fun isPartOfExistingVariable(
        varName: String,
        variables: Map<String, GroovyMarkupVariable>
    ): Boolean {
        return variables.keys.any { it.startsWith("$varName.") }
    }
}