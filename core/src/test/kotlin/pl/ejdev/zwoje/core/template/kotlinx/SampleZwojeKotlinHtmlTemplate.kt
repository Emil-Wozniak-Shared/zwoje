package pl.ejdev.zwoje.core.template.kotlinx

import kotlinx.html.*
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.kotlinx.ZwojeKotlinHtmlTemplate

class SampleTemplateInputData(
    input: List<String>
): TemplateInputData<List<String>>(input)

object SampleZwojeKotlinHtmlTemplate : ZwojeKotlinHtmlTemplate<List<String>>() {
    override fun compile(input: TemplateInputData<List<String>>) = html("Users") {
        val users = input.data
        body {
            div {
                h2 { +"USERS" }
                table {
                    thead {
                        tr {
                            th(ThScope.col) { +"id" }
                            th(ThScope.col) { +"Name" }
                        }
                    }
                    tbody { usersRows(users) }
                }
            }
        }
    }

    private fun TBODY.usersRows(users: List<String>) {
        users.forEachIndexed { id, user ->
            tr {
                td { +id.toString() }
                td { +user }
            }
        }
    }

}

