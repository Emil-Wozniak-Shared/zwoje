package pl.ejdev.zwoje.core.template.kotlinx

import kotlinx.html.HTML
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate

abstract class ZwojeKotlinHtmlTemplate<INPUT : Any> : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {
    override val templatePath: String? = null

    protected fun html(title: String, block: HTML.() -> Unit) = createHTML().html {
        head {
            title(title)
        }
        block()
    }
}