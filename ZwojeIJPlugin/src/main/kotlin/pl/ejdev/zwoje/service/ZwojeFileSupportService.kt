package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import pl.ejdev.zwoje.core.template.TemplateType

@Service(Service.Level.PROJECT)
class ZwojeFileSupportService {
    fun isSupported(extension: String) = extension in supportedTypes

    private companion object {
        val supportedTypes: Set<String> = TemplateType.entries.map { it.extension }.toSet()
    }

}