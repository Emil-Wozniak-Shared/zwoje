package pl.ejdev.zwoje.core.template

abstract class ZwojeTemplateResolver<DATA> where DATA : Any {
    abstract val type: TemplateType

    abstract fun<T: Any> register(id: String, template: @UnsafeVariance ZwojeTemplate<TemplateInputData<T>, T>)

    abstract operator fun get(id: String): ZwojeTemplate<TemplateInputData<DATA>, DATA>
}