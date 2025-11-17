package pl.ejdev.zwoje.components

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import pl.ejdev.zwoje.service.ZwojeConfigFileService

class ZwojeTemplateList(
    private val project: Project,
    keys: Array<String>
) : ComboBox<String>(keys) {

    private val zwojeConfigFileService = project.service<ZwojeConfigFileService>()

    init {
        zwojeConfigFileService.getConfigs().forEach {
            it.config.templates.keys.forEach { key -> this.addItem(key) }
        }
    }
}