package auth.model.openid

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter

@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonSetter("authorization_info_token")
    var authorizationInfoToken: String? = null
)
