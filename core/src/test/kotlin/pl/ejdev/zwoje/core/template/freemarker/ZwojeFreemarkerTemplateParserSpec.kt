package pl.ejdev.zwoje.core.template.freemarker

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldNotBeEmpty
import org.intellij.lang.annotations.Language
import pl.ejdev.zwoje.core.template.VariableType

class ZwojeFreemarkerTemplateParserSpec : FreeSpec({
    "should parse template with simple fields" - {
        @Language("html")
        val content = $$"""
     <!DOCTYPE html>
        <html>
        <head>
            <title>Invoice</title>
        </head>
        <body>
        <h1>Invoice for <span>${name}</span></h1>
        <p>Amount due: $<span>${amount}</span></p>
        </body>
        </html>
        """.trimIndent()

        val variables = ZwojeFreemarkerTemplateParser.parse(content).toList()

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
            <h1>Invoice for <span>${name}</span></h1>
            <p>Amount due: $<span>${amount}</span></p>
            <#list invoice.items as item>
            <ul>
                <li>${item.name}</li>
                <li>${item.description}</li>
                <li>${item.quantity}</li>
                <li>${item.unitPrice}</li>
                <li>${item.total}</li>
            </ul>
            </#list>
            </body>
            </html>
        """.trimIndent()

        val variables = ZwojeFreemarkerTemplateParser.parse(content).toList()

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
