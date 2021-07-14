package auth.model.corppass

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

data class UserInfo(
    var accountType: String = "User",
    var userId: String = "",
    var userCountry: String = "SG",
    var userFullName: String = "",
    var userName: String = "",
    var singpassHolder: Boolean = false,
    var entityId: String = "",
    var entityStatus: String? = null,
    var entityType: String = "UEN",
    var entityRegNo: String? = null,
    var entityCountry: String? = null,
    var entityName: String? = null
) {
    constructor(mapData: Map<String, Any>) : this(
        mapData.get("accountType") as? String ?: "",
        mapData.get("userId") as? String ?: "",
        mapData.get("userCountry") as? String ?: "",
        mapData.get("userFullName") as? String ?: "",
        mapData.get("userName") as? String ?: "",
        mapData.get("singpassHolder") as? Boolean ?: false,
        mapData.get("entityId") as? String ?: "",
        mapData.get("entityStatus") as? String,
        mapData.get("entityType") as? String ?: "",
        mapData.get("entityRegNo") as? String,
        mapData.get("entityCountry") as? String,
        mapData.get("entityName") as? String
    )

    fun toMap(): Map<String, Any?> {
        val mapper = ObjectMapper()
        return mapper.convertValue(this, object : TypeReference<Map<String, Any?>>() {})
    }

    fun toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
