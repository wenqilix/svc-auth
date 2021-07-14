package auth.service.corppass

import auth.model.corppass.User
import auth.model.corppass.UserInfo
import auth.model.openid.TokenResponse
import auth.model.openid.corppass.TokenAuthorizationClaim
import auth.model.openid.corppass.TokenUserInfoClaim
import auth.service.BaseOpenIdTokenService
import auth.util.helper.OpenIdProvider
import auth.util.helper.Properties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("CorppassOpenIdTokenService")
class OpenIdTokenService : BaseOpenIdTokenService<User>() {

    private val logger = LoggerFactory.getLogger(OpenIdTokenService::class.java)
    @Autowired
    internal lateinit var properties: Properties

    internal override val openIdProvider: OpenIdProvider?
        get() {
            return properties.corppass.openIdProvider
        }

    override fun parse(tokenResponse: TokenResponse): User {
        try {
            val authorizationInfoPayload = tokenResponse.authorizationInfoToken?.let {
                tokenParser.parse(it)
            } ?: emptyMap()
            val idTokenPayload = tokenParser.parse(tokenResponse.idToken)

            logger.debug(
                "Corppass openId TokenResponse.idToken: {}, TokenResponse.authorizationInfoToken: {}",
                idTokenPayload,
                authorizationInfoPayload
            )

            val mapper = ObjectMapper()
            val tokenUserInfo = mapper.convertValue(idTokenPayload, TokenUserInfoClaim::class.java)
            val tokenAuthorization = mapper.convertValue(authorizationInfoPayload, TokenAuthorizationClaim::class.java)

            val userInfo = UserInfo(
                accountType = tokenUserInfo.userInfo.accountType,
                userId = tokenUserInfo.subjectInfo.userId,
                userCountry = tokenUserInfo.subjectInfo.userCountry,
                userFullName = tokenUserInfo.userInfo.userFullName,
                userName = tokenUserInfo.subjectInfo.userName,
                singpassHolder = tokenUserInfo.userInfo.singpassHolder,
                entityId = tokenUserInfo.entityInfo.entityId,
                entityStatus = tokenUserInfo.entityInfo.entityStatus,
                entityType = tokenUserInfo.entityInfo.entityType,
                entityRegNo = tokenUserInfo.entityInfo.entityRegNo,
                entityCountry = tokenUserInfo.entityInfo.entityCountry,
                entityName = tokenUserInfo.entityInfo.entityName
            )
            return User(
                userInfo = userInfo,
                authAccess = tokenAuthorization.authAccess,
                thirdPartyAuthAccess = tokenAuthorization.thirdPartyAuthAccess,
                refreshToken = tokenResponse.refreshToken
            )
        } catch (e: Exception) {
            logger.error("Error in parsing TokenResponse")
            throw e
        }
    }
}
