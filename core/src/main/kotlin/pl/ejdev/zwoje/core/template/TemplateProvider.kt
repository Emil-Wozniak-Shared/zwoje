package pl.ejdev.zwoje.core.template

interface TemplateProvider {
    val baseDir: String
    val templatesDir: String
    val extension: String
}