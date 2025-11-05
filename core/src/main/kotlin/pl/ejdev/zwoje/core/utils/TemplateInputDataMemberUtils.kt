package pl.ejdev.zwoje.core.utils

import pl.ejdev.zwoje.core.template.TemplateInputData

@Suppress("NO_REFLECTION_IN_CLASS_PATH")
fun<T: Any> TemplateInputData<*>.getMembers(): List<Pair<String, Any?>> {
    val data = this.data
    val visited = mutableSetOf<Any>()
    return collectMembers("", data, visited)
}


//@Suppress("NO_REFLECTION_IN_CLASS_PATH")
//fun <T : Any> TemplateInputData<T>.getMembers(): List<Pair<String, Any?>> {
//    val data = this.data ?: return emptyList()
//    val visited = mutableSetOf<Any>()
//    return collectMembers("", data, visited)
//}

private fun collectMembers(prefix: String, value: Any?, visited: MutableSet<Any>): List<Pair<String, Any?>> {
    if (value == null) return emptyList()
    if (!visited.add(value)) return emptyList() // avoid recursion cycles

    return when (value) {
        is Map<*, *> -> {
            value.entries.filter { it.key != null }.flatMap { (k, v) ->
                val key = k.toString()
                val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
                collectMembers(fullKey, v, visited) + (fullKey to v)
            }
        }

        is Collection<*> -> {
            val listKey = prefix.ifEmpty { "items" }
            val indexed = value.flatMapIndexed { index, element ->
                val itemKey = "$listKey[$index]"
                collectMembers(itemKey, element, visited)
            }
            indexed + (listKey to value)
        }

        else -> {
            value::class
                .members
                .filter(dataClassMembersFilter)
                .mapNotNull { member ->
                    member.runCatching { name to this.call(this@runCatching) }.getOrNull()
                }
        }
    }
}
