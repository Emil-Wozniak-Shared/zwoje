package pl.ejdev.zwoje.core.template.jasper

import net.sf.jasperreports.engine.JREmptyDataSource
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateOutput
import pl.ejdev.zwoje.core.template.ZwojeTemplate

private const val IMAGE_ERROR_MSG = """
    Zwoje could not compile JasperImage evaluation using classpath.

    Please set image path using 'IMAGE_DIR' parameter.
    <imageExpression><![CDATA[$\{'$'}P{IMAGE_DIR} + "/images/logo.png"]]></imageExpression>
                        
    then add below to the samples json:
    
    {"samples":[{
        "IMAGE_DIR": "/<project-path>/src/main/resources"
    }]}
"""
private const val JASPER_IMAGE_CLASS = "net.sf.jasperreports.engine.fill.JRFillImage"
private const val EVALUATE_IMAGE = "evaluateImage"

abstract class  JasperTemplate<C : Any>(
    override val templatePath: String
): ZwojeTemplate<TemplateInputData<C>,C> {

    @Suppress("UNCHECKED_CAST")
    override fun compile(input: TemplateInputData<C>): TemplateOutput {
        val jasperReport = JasperCompileManager.compileReport(templatePath)
        val data = (input.data as Map<String, Any>).toMutableMap()
        try {
            val dataSource = JREmptyDataSource() // or JDBC connection
            val print: JasperPrint = JasperFillManager.fillReport(jasperReport, data, dataSource)
            return TemplateOutput.JasperFill(print)
        } catch (e: Exception) {
            if (e.containsJasperImageEvaluation()) {
                throw JasperImageException(IMAGE_ERROR_MSG.trimIndent(), e)
            }
            throw e
        }
    }

    fun Throwable.containsJasperImageEvaluation(): Boolean =
        stackTrace.any { it.className == JASPER_IMAGE_CLASS && it.methodName == EVALUATE_IMAGE }

    class JasperImageException(message: String, exception: Exception) : RuntimeException(message, exception)
}
