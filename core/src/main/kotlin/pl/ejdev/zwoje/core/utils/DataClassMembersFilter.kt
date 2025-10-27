package pl.ejdev.zwoje.core.utils

import kotlin.reflect.KCallable

val dataClassMembersFilter: (KCallable<*>) -> Boolean = {
    !it.name.startsWith("component") &&
            !it.name.startsWith("hashCode") &&
            !it.name.startsWith("toString")
}
