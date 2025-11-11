package pl.ejdev.zwoje.core.template.mustache

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldNotBeEmpty
import org.intellij.lang.annotations.Language
import pl.ejdev.zwoje.core.template.VariableType

class ZwojeMustacheTemplateParserSpec : FreeSpec({

    "should parse template with simple fields" - {
        @Language("html")
        val content = $$"""
           <html>
            <head><title>Invoice</title></head>
            <body>
              <h1>Invoice for {{name}}</h1>
              <p>Amount due: ${{amount}}</p>
            </body>
            </html>
        """.trimIndent()

        val variables = ZwojeMustacheTemplateParser.parse(content).toList()

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
            <h1>Invoice for <span>{{name}}</span></h1>
            <p>Amount due: $<span>{{amount}}</span></p>
            {{#invoice.items}}
            <ul>
                <li>{{name}}</li>
                <li>{{description}}</li>
                <li>{{quantity}}</li>
                <li>{{unitPrice}}</li>
                <li>{{total}}</li>
            </ul>
            {{/invoice.items}}
            </body>
            </html>
        """.trimIndent()

        val variables = ZwojeMustacheTemplateParser.parse(content).toList()

        variables.shouldNotBeEmpty()
        variables.size `should be equal to` 3

        variables[0].type `should be` VariableType.SINGLE
        variables[0].name `should be equal to` "name"
        variables[1].type `should be` VariableType.SINGLE
        variables[1].name `should be equal to` "amount"
        variables[2].type `should be` VariableType.COLLECTION
        variables[2].name `should be equal to` "invoice.items"
        variables[2].children.size `should be equal to` 5
    }

})
