package auth.model.corppass.auth

import auth.util.helper.json.FlatMapResultSetJsonDeserializer
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class ThirdPartyClient(
    @JsonSetter("CP_Clnt_ID", nulls = Nulls.AS_EMPTY)
    var entityId: String = "",

    @JsonSetter("CP_ClntEnt_TYPE", nulls = Nulls.AS_EMPTY)
    var entityType: String = "",

    @JsonDeserialize(using = FlatMapResultSetJsonDeserializer::class)
    @JsonSetter("Auth_Result_Set", nulls = Nulls.AS_EMPTY)
    @JsonAlias("auths")
    var auths: MutableList<Auth> = mutableListOf<Auth>()
) {
    constructor(mapData: Map<String, Any>) : this(
        mapData.get("entityId") as? String ?: "",
        mapData.get("entityType") as? String ?: "",
        (mapData.get("auths") as? List<Map<String, Any>> ?: listOf<Map<String, Any>>()).map {
            auth ->
            Auth(auth)
        }.toMutableList()
    )

    fun toMap(): Map<String, Any> {
        return mapOf(
            "entityId" to this.entityId,
            "entityType" to this.entityType,
            "auths" to this.auths.map { auth -> auth.toMap() }
        )
    }
}
