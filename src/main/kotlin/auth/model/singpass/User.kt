package auth.model.singpass

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = [JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES])
data class User(
    val userName: String = "",
    val refreshToken: String? = null,
    val iat: Long? = null
) {
    constructor(mapData: Map<String, Any>) : this(
        mapData.get("userName") as? String ?: "",
        mapData.get("refreshToken") as? String?,
        mapData.get("iat") as? Long?
    )

    fun toMap(): Map<String, Any> {
        return listOfNotNull(
            "userName" to this.userName,
            this.refreshToken?.let { "refreshToken" to this.refreshToken },
            this.iat?.let { "iat" to this.iat }
        ).toMap()
    }
}
