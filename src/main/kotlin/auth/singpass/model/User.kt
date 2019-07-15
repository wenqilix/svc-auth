package auth.singpass.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference

data class User(
    var userName: String = "",
    var mobile: String = ""
) {
    constructor(mapData: Map<String, Any>) : this(
        mapData.get("userName") as? String ?: "",
        mapData.get("mobile") as? String ?: ""
    )

    fun toMap(): Map<String, Any> {
        val mapper = ObjectMapper()
        return mapper.convertValue(this, object : TypeReference<Map<String, Any>>() {})
    }
}
