package auth.model.corppass.auth

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

data class Parameter(
    var value: String = "",
    var name: String = ""
) {
    constructor(mapData: Map<String, String>) : this(
        mapData.getOrDefault("value", ""),
        mapData.getOrDefault("name", "")
    )

    fun toMap(): Map<String, String> {
        val mapper = ObjectMapper()
        return mapper.convertValue(this, object : TypeReference<Map<String, String>>() {})
    }
}
