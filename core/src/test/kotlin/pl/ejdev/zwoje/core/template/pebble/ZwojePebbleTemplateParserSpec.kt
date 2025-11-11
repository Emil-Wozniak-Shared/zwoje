package pl.ejdev.zwoje.core.template.pebble

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldNotBeEmpty
import org.intellij.lang.annotations.Language
import pl.ejdev.zwoje.core.template.VariableType

class ZwojePebbleTemplateParserSpec : FreeSpec({

    "should parse template with simple fields" - {
        @Language("html")
        val content = $$"""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Invoice</title>
            </head>
            <body>
                <h1>Invoice for {{ name }}</h1>
                <p>Amount due: ${{ amount }}</p>
            </body>
            </html>
        """.trimIndent()

        val variables = ZwojePebbleTemplateParser.parse(content).toList()

        variables.shouldNotBeEmpty()
        variables.size `should be equal to` 2

        variables[0].type `should be` VariableType.SINGLE
        variables[0].name `should be equal to` "name"
        variables[1].type `should be` VariableType.SINGLE
        variables[1].name `should be equal to` "amount"
    }

    "should parse template with simple fields and collection fields" - {
        @Language("html")
        val content = $$"""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Invoice</title>
            </head>
            <body>
                <h1>Invoice for {{ name }}</h1>
                <p>Amount due: ${{ amount }}</p>
            
                {% if amount > 100 %}
                  <p><strong>High-value invoice!</strong></p>
                {% endif %}
            
                <ul>
                  {% for item in items %}
                    <li>{{ item.name }}</li>
                    <li>{{ item.description }}</li>
                    <li>{{ item.unitPrice }}</li>
                    <li>{{ item.total }}</li>
                  {% endfor %}
                </ul>
            </body>
            </html>
        """.trimIndent()

        val variables = ZwojePebbleTemplateParser.parse(content).toList()

        variables.shouldNotBeEmpty()
        variables.size `should be equal to` 3

        variables[0].type `should be` VariableType.SINGLE
        variables[0].name `should be equal to` "name"
        variables[1].type `should be` VariableType.SINGLE
        variables[1].name `should be equal to` "amount"
        variables[2].type `should be` VariableType.COLLECTION
        variables[2].name `should be equal to` "items"
        variables[2].children.size `should be equal to` 4
    }
})
