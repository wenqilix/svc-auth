package auth.corppass.model

import javax.xml.bind.annotation.*

@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.FIELD)
data class User (
    @field:XmlElement(name = "UserInfo") val userInfo: UserInfo = UserInfo(),
    @field:XmlElement(name = "AuthAccess") val authAccess: AuthAccess = AuthAccess(),
    @field:XmlElement(name = "TPAuthAccess") val thirdPartyAuthAccess: ThirdPartyAuthAccess = ThirdPartyAuthAccess()
) {
    constructor(mapData: Map<String, Any>): this(
        UserInfo(mapData.get("userInfo") as? Map<String, Any> ?: mapOf<String, Any>()),
        AuthAccess(mapData.get("authAccess") as? Map<String, Any> ?: mapOf<String, Any>()),
        ThirdPartyAuthAccess(mapData.get("thirdPartyAuthAccess") as? Map<String, Any> ?: mapOf<String, Any>())
    )

    fun toMap(): Map<String, Any> {
        return mapOf(
            "userInfo" to this.userInfo.toMap(),
            "authAccess" to this.authAccess.toMap(),
            "thirdPartyAuthAccess" to this.thirdPartyAuthAccess.toMap()
        )
    }
}
