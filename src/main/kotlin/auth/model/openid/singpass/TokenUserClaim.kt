package auth.model.openid.singpass

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenUserClaim(
    @JsonSetter("sub")
    val sub: String
) {
    val subjectInfo: SubjectInfo
        get() {
            val subjectInfoMap = sub.split(",").map {
                it.split("=").first() to it.split("=").last()
            }.toMap()
            return SubjectInfo(subjectInfoMap)
        }
}

data class SubjectInfo(
    val userName: String,
    val uuid: String
) {
    constructor(mapData: Map<String, Any>) : this(
        mapData.get("s") as? String ?: "",
        mapData.get("u") as? String ?: ""
    )
}
