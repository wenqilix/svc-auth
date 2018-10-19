package auth.service.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Service(
    @JsonProperty("guid") var guid: String = "",
    @JsonProperty("public-key") var publicKey: String = "",
    @JsonProperty("payload") var payload: Map<String, Any>
) {
    constructor(mapData: Map<String, Any>): this(
        mapData.get("guid") as? String ?: "",
        mapData.get("publicKey") as? String ?: "",
        mapData.get("payload") as? Map<String, Any> ?: emptyMap<String, Any>()
    )
}
