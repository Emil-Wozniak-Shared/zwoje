package pl.ejdev.zwoje.core.engine

abstract class PdfCompileEngine {
    abstract fun compile(template: String): ByteArray
}