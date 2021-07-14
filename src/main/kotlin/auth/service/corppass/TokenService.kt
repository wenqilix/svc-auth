package auth.service.corppass

import auth.model.corppass.User
import auth.service.TokenService
import org.springframework.stereotype.Service

const val ISSUER = "Corppass"

@Service("CorppassTokenService")
class TokenService : TokenService() {
    fun parse(jwt: String): User {
        val attributes = parse(ISSUER, jwt)
        return User(attributes)
    }

    fun build(user: User): String {
        val token = build(ISSUER, user.toMap())

        val additionalInfo = properties.corppass.additionalInfoRequest.resolveAdditionalInfo(
            mapOf("Authorization" to "Bearer $token")
        )

        if (additionalInfo != null) {
            val claimsWithAdditionalInfo = user.toMap().plus(mapOf("additionalInfo" to additionalInfo))
            return build(ISSUER, claimsWithAdditionalInfo)
        }
        return token
    }
}
