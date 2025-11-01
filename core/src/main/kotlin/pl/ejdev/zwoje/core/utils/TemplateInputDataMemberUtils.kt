package pl.ejdev.zwoje.core.utils

import pl.ejdev.zwoje.core.template.TemplateInputData

@Suppress("NO_REFLECTION_IN_CLASS_PATH")
fun<T: Any> TemplateInputData<*>.getMembers() = this.data::class
    .members
    .filter(dataClassMembersFilter)
    .mapNotNull { member ->
        member.runCatching { name to this.call(this@getMembers) }.getOrNull()
    }

