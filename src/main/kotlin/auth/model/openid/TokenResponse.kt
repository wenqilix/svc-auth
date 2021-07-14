package auth.model.openid

import com.fasterxml.jackson.annotation.JsonSetter

data class TokenResponse(
    @JsonSetter("access_token")
    val accessToken: String,
    @JsonSetter("refresh_token")
    val refreshToken: String?,
    @JsonSetter("id_token")
    val idToken: String,
    @JsonSetter("token_type")
    val tokenType: String,
    @JsonSetter("expires_in")
    val expiresIn: Int,
    @JsonSetter("scope")
    val scope: String,
    @JsonSetter("authorization_info_token")
    var authorizationInfoToken: String? = null
)
