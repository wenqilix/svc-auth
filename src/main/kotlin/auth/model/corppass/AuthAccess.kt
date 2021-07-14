package auth.model.corppass

import auth.model.corppass.auth.Auth
import auth.util.helper.json.FlatMapResultSetJsonDeserializer
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class AuthAccess(
    @JsonDeserialize(using = FlatMapResultSetJsonDeserializer::class)
    @JsonSetter("Result_Set")
    @JsonAlias("auths")
    var auths: MutableList<Auth> = mutableListOf<Auth>()
) {
    constructor(mapData: Map<String, Any>) : this(
        (mapData.get("auths") as? List<Map<String, Any>> ?: listOf<Map<String, Any>>()).map {
            auth ->
            Auth(auth)
        }.toMutableList()
    )

    fun toMap(): Map<String, Any> {
        return mapOf(
            "auths" to this.auths.map { auth -> auth.toMap() }
        )
    }

    fun toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
