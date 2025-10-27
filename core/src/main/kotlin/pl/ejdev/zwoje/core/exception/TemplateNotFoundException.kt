package pl.ejdev.zwoje.core.exception

class TemplateNotFoundException(
    id: String
): RuntimeException("template with id: '$id' not found")