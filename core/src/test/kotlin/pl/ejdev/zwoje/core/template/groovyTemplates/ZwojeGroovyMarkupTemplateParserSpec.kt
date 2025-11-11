package pl.ejdev.zwoje.core.template.groovyTemplates

import io.kotest.core.spec.style.FreeSpec
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldNotBeEmpty
import org.intellij.lang.annotations.Language
import pl.ejdev.zwoje.core.template.VariableType

class ZwojeGroovyMarkupTemplateParserSpec : FreeSpec({
    "should parse template with simple fields" - {
        @Language("groovy")
        val content = """
        yieldUnescaped '<!DOCTYPE html>'
        html {
            head {
                title('Invoice')
            }
            body {
                h1 {
                    yield 'Invoice for '
                    span(name)
                }
                p {
                    yield 'Amount due: $'
                    span(amount)
                }
            }
        }
        """.trimIndent()

        val variables = ZwojeGroovyMarkupTemplateParser.parse(content).toList()

        variables.shouldNotBeEmpty()
        variables.size `should be equal to` 2

        variables[0].type `should be` VariableType.SINGLE
        variables[0].name `should be equal to` "name"
        variables[1].type `should be` VariableType.SINGLE
        variables[1].name `should be equal to` "amount"
    }

    "should parse template with simple fields and collection fields" - {
        @Language("groovy")
        val content = """
            yieldUnescaped '<!DOCTYPE html>'
            html {
                head {
                    title('Invoice')
                }
                body {
                    h1 {
                        yield 'Invoice for '
                        span(name)
                    }
                    p {
                        yield 'Amount due: $'
                        span(amount)
                    }
                    invoice.items.each { item ->
                        ul {
                            li(item.name)
                            li(item.description)
                            li(item.quantity)
                            li(item.unitPrice)
                            li(item.total)
                        }
                    }
                }
            }
        """.trimIndent()

        val variables = ZwojeGroovyMarkupTemplateParser.parse(content).toList()

        variables.shouldNotBeEmpty()
        variables.size `should be equal to` 4

        variables[0].type `should be` VariableType.OBJECT
        variables[0].name `should be equal to` "invoice"
        variables[1].type `should be` VariableType.COLLECTION
        variables[1].name `should be equal to` "invoice.items"
        variables[2].type `should be` VariableType.SINGLE
        variables[2].name `should be equal to` "name"
        variables[3].type `should be` VariableType.SINGLE
        variables[3].name `should be equal to` "amount"
    }

})
