package auth.model.openid.corppass

import auth.model.corppass.AuthAccess
import auth.model.corppass.ThirdPartyAuthAccess
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenAuthorizationClaim(
    @JsonSetter("AuthInfo", nulls = Nulls.AS_EMPTY)
    val authAccess: AuthAccess,
    @JsonSetter("TPAuthInfo", nulls = Nulls.AS_EMPTY)
    val thirdPartyAuthAccess: ThirdPartyAuthAccess
)
