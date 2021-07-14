package auth.model.corppass

data class User(
    val userInfo: UserInfo = UserInfo(),
    val authAccess: AuthAccess = AuthAccess(),
    val thirdPartyAuthAccess: ThirdPartyAuthAccess = ThirdPartyAuthAccess(),
    val refreshToken: String? = null,
    val iat: Long? = null
) {
    constructor(mapData: Map<String, Any>) : this(
        UserInfo(mapData.get("userInfo") as? Map<String, Any> ?: mapOf<String, Any>()),
        AuthAccess(mapData.get("authAccess") as? Map<String, Any> ?: mapOf<String, Any>()),
        ThirdPartyAuthAccess(mapData.get("thirdPartyAuthAccess") as? Map<String, Any> ?: mapOf<String, Any>()),
        mapData.get("refreshToken") as? String?,
        mapData.get("iat") as? Long?
    )

    fun toMap(): Map<String, Any> {
        return listOfNotNull(
            "userInfo" to this.userInfo.toMap(),
            "authAccess" to this.authAccess.toMap(),
            "thirdPartyAuthAccess" to this.thirdPartyAuthAccess.toMap(),
            this.refreshToken?.let { "refreshToken" to this.refreshToken },
            this.iat?.let { "iat" to this.iat }
        ).toMap()
    }
}
