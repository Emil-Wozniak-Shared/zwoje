package pl.ejdev.zwoje.core.utils

import pl.ejdev.zwoje.core.template.TemplateInputData

fun TemplateInputData<*>.getMembers(): List<Pair<String, Any?>> {
    val data = this.data
    val visited = mutableSetOf<Any>()
    return this.collectMembers("", data, visited)
}

@Suppress("NO_REFLECTION_IN_CLASS_PATH")
private fun TemplateInputData<*>.collectMembers(
    prefix: String,
    value: Any?,
    visited: MutableSet<Any>
): List<Pair<String, Any?>> {
    if (value == null) return emptyList()
    if (!visited.add(value)) return emptyList() // avoid recursion cycles
    return when (value) {
        is Map<*, *> -> value.entries
            .filter { it.key != null }
            .flatMap { (key, value) ->
                val key = key.toString()
                val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
                collectMembers(fullKey, value, visited) + (fullKey to value)
            }

        is Collection<*> -> {
            val listKey = prefix.ifEmpty { "items" }
            val indexed = value.flatMapIndexed { index, element ->
                val itemKey = "$listKey[$index]"
                collectMembers(itemKey, element, visited)
            }
            indexed + (listKey to value)
        }

        else -> value::class
            .members
            .filter(dataClassMembersFilter)
            .mapNotNull { member ->
                member.runCatching { name to this.call(this@collectMembers) }.getOrNull()
            }
    }
}
