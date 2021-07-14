package auth.model.corppass

import auth.model.corppass.auth.ThirdPartyClient
import auth.util.helper.json.FlatMapResultSetJsonDeserializer
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class ThirdPartyAuthAccess(
    @JsonDeserialize(using = FlatMapResultSetJsonDeserializer::class)
    @JsonSetter("Result_Set")
    @JsonAlias("clients")
    var clients: MutableList<ThirdPartyClient> = mutableListOf<ThirdPartyClient>()
) {
    constructor(mapData: Map<String, Any>) : this(
        (mapData.get("clients") as? List<Map<String, Any>> ?: listOf<Map<String, Any>>()).map {
            client ->
            ThirdPartyClient(client)
        }.toMutableList()
    )

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "clients" to this.clients.map { client -> client.toMap() }
        )
    }

    fun toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
