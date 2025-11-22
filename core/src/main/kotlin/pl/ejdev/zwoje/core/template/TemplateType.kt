package pl.ejdev.zwoje.core.template

enum class TemplateType(
    val artifactName: String,
    val extension: String
) {
    FreeMarker("freemarker", "ftl"),
    GroovyTemplate("groovy-templates", "gtmpl"),
    Mustache("mustache", "mustache"),
    Pebble("pebble", "peb"),
    KotlinxHtml("kotlinx-html-jvm", "kt"),
    Thymeleaf("thymeleaf",  "html"),
    ApacheVM("apache-vm", "vm")
}