package pl.ejdev.zwoje.core.template.thymeleaf

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.ExpressionContext
import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.standard.expression.StandardExpressions
import org.thymeleaf.standard.expression.VariableExpression
import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateParser
import java.util.*

class ThymeleafVariable(
    name: String,
    type: VariableType
) : TemplateVariable(name, type)

class ZwojeThymeleafTemplateParser<INPUT : Any>(
    private val engine: TemplateEngine
) : ZwojeTemplateParser<INPUT> {
    override fun parse(content: String): Set<TemplateVariable> {
        val context: IExpressionContext = ExpressionContext(engine.configuration, Locale.getDefault())

        val config = engine.configuration
        val parser = StandardExpressions.getExpressionParser(config)
        val variables = mutableSetOf<ThymeleafVariable>()

        // Detect collections used in th:each
        val eachRegex = Regex("""th:each\s*=\s*"(\s*\w+)\s*:\s*\$\{([^}]+)}""")
        val loopVars = mutableSetOf<String>() // to track local iteration variable names

        eachRegex.findAll(content).forEach { match ->
            val loopVar = match.groupValues[1].trim()
            val expr = match.groupValues[2].trim()
            val root = expr.substringBefore(".").substringBefore("[")
            loopVars.add(loopVar)
            if (root.isNotBlank()) {
                variables.add(ThymeleafVariable(root, VariableType.COLLECTION))
            }
        }

        // Find all normal variable expressions
        val exprRegex = Regex("""\$\{([^}]+)}|\*\{([^}]+)}""")
        exprRegex.findAll(content).forEach { match ->
            val exprText = match.groups[1]?.value ?: match.groups[2]?.value ?: return@forEach
            val parsedExpression = parser.parseExpression(context, exprText)

            val root = if (parsedExpression is VariableExpression) {
                parsedExpression.expression
            } else {
                exprText.substringBefore(".").substringBefore("[")
            }

            // Skip local loop variables (like "item")
            if (root.isNotBlank() && root !in loopVars && variables.none { it.name == root }) {
                variables.add(ThymeleafVariable(root, VariableType.SINGLE))
            }
        }

        return variables
    }
}