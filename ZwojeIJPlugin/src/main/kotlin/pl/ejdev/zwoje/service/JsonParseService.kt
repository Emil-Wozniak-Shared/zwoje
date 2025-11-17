package pl.ejdev.zwoje.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class JsonParseService {
    private val gson = Gson()

    internal inline fun <reified T> parse(content: String): T {
        val json = gson.fromJson(content, T::class.java)
        return json
    }

    internal inline fun <reified T> toJson(data: T): String {
        val json = gson.toJson(data)
        return json
    }
}