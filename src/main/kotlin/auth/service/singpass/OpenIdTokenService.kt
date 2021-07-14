package auth.service.singpass

import auth.model.openid.TokenResponse
import auth.model.openid.singpass.TokenUserClaim
import auth.model.singpass.User
import auth.service.BaseOpenIdTokenService
import auth.util.helper.OpenIdProvider
import auth.util.helper.Properties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("SingpassOpenIdTokenService")
class OpenIdTokenService : BaseOpenIdTokenService<User>() {
    private val logger = LoggerFactory.getLogger(OpenIdTokenService::class.java)
    @Autowired
    internal lateinit var properties: Properties

    internal override val openIdProvider: OpenIdProvider?
        get() {
            return properties.singpass.openIdProvider
        }

    override fun parse(tokenResponse: TokenResponse): User {
        try {
            val idTokenPayload = tokenParser.parse(tokenResponse.idToken)

            logger.debug(
                "Singpass openId TokenResponse.idToken: {}",
                idTokenPayload
            )

            val mapper = ObjectMapper()
            val tokenUser = mapper.convertValue(idTokenPayload, TokenUserClaim::class.java)

            return User(
                userName = tokenUser.subjectInfo.userName,
                refreshToken = tokenResponse.refreshToken
            )
        } catch (e: Exception) {
            logger.error("Error in parsing TokenResponse")
            throw e
        }
    }
}
