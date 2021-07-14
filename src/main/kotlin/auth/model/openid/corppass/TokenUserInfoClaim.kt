package auth.model.openid.corppass

import auth.util.helper.json.BooleanStringJsonDeserializer
import auth.util.helper.json.EmptyStringJsonDeserializer
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenUserInfoClaim(
    @JsonSetter("userInfo")
    val userInfo: UserInfo,
    @JsonSetter("entityInfo")
    val entityInfo: EntityInfo,
    @JsonSetter("sub")
    val sub: String
) {
    val subjectInfo: SubjectInfo
        get() {
            val subjectInfoMap = sub.split(",").map {
                it.trim().split("=").first() to it.split("=").last()
            }.toMap()
            return SubjectInfo(subjectInfoMap)
        }
}

data class UserInfo(
    @JsonSetter("CPAccType")
    val accountType: String,
    @JsonSetter("CPUID_FullName")
    val userFullName: String,
    @JsonDeserialize(using = BooleanStringJsonDeserializer::class)
    @JsonSetter("ISSPHOLDER")
    val singpassHolder: Boolean
)

data class EntityInfo(
    @JsonSetter("CPEntID")
    val entityId: String,
    @JsonDeserialize(using = EmptyStringJsonDeserializer::class)
    @JsonSetter("CPEnt_Status")
    val entityStatus: String?,
    @JsonSetter("CPEnt_TYPE")
    val entityType: String,
    @JsonDeserialize(using = EmptyStringJsonDeserializer::class)
    @JsonSetter("CPNonUEN_RegNo")
    val entityRegNo: String?,
    @JsonDeserialize(using = EmptyStringJsonDeserializer::class)
    @JsonSetter("CPNonUEN_Country")
    val entityCountry: String?,
    @JsonDeserialize(using = EmptyStringJsonDeserializer::class)
    @JsonSetter("CPNonUEN_Name")
    val entityName: String?
)

data class SubjectInfo(
    val userId: String,
    val userName: String,
    val userCountry: String
) {
    constructor(mapData: Map<String, Any>) : this(
        mapData.get("s") as? String ?: "",
        mapData.get("u") as? String ?: "",
        mapData.get("c") as? String ?: ""
    )
}
