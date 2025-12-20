package pl.ejdev.zwoje.core.engine

import net.sf.jasperreports.engine.JasperExportManager

class JasperCompileEngine : PdfCompileEngine<JasperFill>() {
    override fun compile(compileData: JasperFill): ByteArray =
        JasperExportManager.exportReportToPdf(compileData.print)
}
