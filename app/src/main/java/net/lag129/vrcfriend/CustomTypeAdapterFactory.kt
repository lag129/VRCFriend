package net.lag129.vrcfriend

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.vrchatapi.model.CurrentUser

class CustomTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != CurrentUser::class.java) {
            return null
        }

        val elementAdapter = gson.getAdapter(JsonElement::class.java)
        val delegateAdapter = gson.getDelegateAdapter(this, type)

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) {
                if (value == null) {
                    out.nullValue()
                    return
                }
                val jsonElement = delegateAdapter.toJsonTree(value)
                elementAdapter.write(out, jsonElement)
            }

            override fun read(`in`: JsonReader): T? {
                val jsonElement = elementAdapter.read(`in`)
                if (jsonElement.isJsonNull) {
                    return null
                }

                val jsonObj = jsonElement.asJsonObject

                val removedFields = mutableListOf<String>()
                if (jsonObj.has("pronounsHistory")) {
                    jsonObj.remove("pronounsHistory")
                    removedFields.add("pronounsHistory")
                }

                return delegateAdapter.fromJsonTree(jsonElement)
            }
        }
    }
}