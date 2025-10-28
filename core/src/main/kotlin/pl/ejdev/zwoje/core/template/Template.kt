package pl.ejdev.zwoje.core.template

enum class Template(
    val artifactName: String
) {
    FreeMarker("freemarker"),
    GroovyTemplate("groovy-templates"),
    Mustache("mustache"),
    Pebble("pebble"),
    KotlinxHtml("kotlinx-html-jvm"),
    Thymeleaf("thymeleaf")
}