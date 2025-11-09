package pl.ejdev.zwoje.core.template.thymeleaf

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldNotBeEmpty
import pl.ejdev.zwoje.core.template.VariableType

class ZwojeThymeleafTemplateParserSpec : FreeSpec({

    "should parse template with simple fields" - {
        val content = $$"""
            <!DOCTYPE html>
            <html xmlns:th="http://www.w3.org/1999/xhtml">
            <head>
                <title>Invoice</title>
            </head>
            <body>
            <h1>Invoice for <span th:text="${name}">Name</span></h1>
            <p>Amount due: $<span th:text="${amount}">0.00</span></p>
            </body>
            </html>
        """.trimIndent()

        val variables = ZwojeThymeleafTemplateParser.parse(content).toList()

        variables.shouldNotBeEmpty()
        variables.size `should be equal to` 2

        variables[0].type `should be` VariableType.SINGLE
        variables[0].name `should be equal to` "name"
        variables[1].type `should be` VariableType.SINGLE
        variables[1].name `should be equal to` "amount"
    }

    "should parse template with simple fields and collection fields" - {
        val content = $$"""
            <!DOCTYPE html>
            <html xmlns:th="http://www.w3.org/1999/xhtml">
            <head>
                <title>Invoice</title>
            </head>
            <body>
            <h1>Invoice for <span th:text="${name}">Name</span></h1>
            <p>Amount due: $<span th:text="${amount}">0.00</span></p>
            <ul th:each="item : ${invoice.items}">
                <li th:text="${item.name}"></li>
                <li th:text="${item.description}"></li>
                <li th:text="${item.quantity}"></li>
                <li th:text="${item.unitPrice}"></li>
                <li th:text="${item.total}"></li>
            </ul>

            </body>
            </html>
        """.trimIndent()

        val variables = ZwojeThymeleafTemplateParser.parse(content).toList()

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
