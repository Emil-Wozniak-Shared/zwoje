package pl.ejdev.zwoje.core.utils

import pl.ejdev.zwoje.core.template.TemplateInputData

fun<T: Any> TemplateInputData<*>.getMembers() = this.data::class
    .members
    .filter(dataClassMembersFilter)
    .mapNotNull { member ->
        member.runCatching { name to this.call(this@getMembers) }.getOrNull()
    }

