package pl.ejdev.zwoje.core.engine

abstract class PdfCompileEngine {
    abstract fun compile(compileData: CompileData): ByteArray
}

data class CompileData(
    val html: String,
    val templatePath: String?
)