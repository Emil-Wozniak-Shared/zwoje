package pl.ejdev.zwoje.core.engine

abstract class PdfCompileEngine<out C : CompileData> {
    abstract fun compile(compileData: @UnsafeVariance C): ByteArray
}

