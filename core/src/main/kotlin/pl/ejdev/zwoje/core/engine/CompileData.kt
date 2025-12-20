package pl.ejdev.zwoje.core.engine

import net.sf.jasperreports.engine.JasperPrint

open class CompileData(
    val templatePath: String?
)

class HtmlOutput(val html: String, templatePath: String?) : CompileData(templatePath)
class JasperFill(val print: JasperPrint, templatePath: String?) : CompileData(templatePath)
