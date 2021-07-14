package auth.service.singpass

import auth.model.singpass.User
import auth.util.helper.Properties
import auth.util.openid.TokenResolver
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.util.Base64
import java.util.UUID

public interface LoginService {
    var properties: Properties
    val tokenService: TokenService

    fun getIdentityProviderLoginDestination(target: String?): String
    fun getToken(authCode: String): String
    fun refreshToken(token: String): String

    fun getIdentityConsumerCallbackDestination(authCode: String, relayState: String): String {
        val uriComponents: UriComponents = UriComponentsBuilder
            .fromHttpUrl(properties.singpass.serviceProvider.loginUrl)
            .queryParam("authCode", "{authCode}")
            .queryParam("relayState", "{relayState}")
            .queryParam("type", "singpass")
            .encode()
            .buildAndExpand(authCode, relayState)
        return uriComponents.toUriString()
    }

    fun computeConsumerState(target: String?): String {
        val mapper = ObjectMapper()
        return String(
            Base64.getEncoder().encode(
                mapper.writeValueAsBytes(
                    mapOf<String, String>("target" to (target ?: properties.singpass.homepageUrl))
                )
            )
        )
    }
}

@Service("SingpassLoginService")
@Profile("qa", "development", "remote-development")
public class DevelopmentLoginService : LoginService {
    @Autowired
    override lateinit var properties: Properties
    @Autowired
    override lateinit var tokenService: TokenService

    override fun getIdentityProviderLoginDestination(target: String?): String {
        val uriComponents: UriComponents = UriComponentsBuilder.newInstance()
            .path("mock/login")
            .queryParam("state", this.computeConsumerState(target))
            .build()
            .encode()
        return uriComponents.toUriString()
    }

    override fun getToken(authCode: String): String {
        val authContent = Base64.getDecoder().decode(authCode.toByteArray())
        val mapper = ObjectMapper()
        val user = mapper.readValue(authContent, User::class.java)

        return tokenService.build(user)
    }

    override fun refreshToken(token: String): String {
        val user = tokenService.parse(token)
        return tokenService.build(user)
    }
}

@Service("SingpassLoginService")
@Profile("uat", "production")
public class ProductionLoginService : LoginService {
    @Autowired
    override lateinit var properties: Properties
    @Autowired
    override lateinit var tokenService: TokenService
    @Autowired
    lateinit var openidTokenService: OpenIdTokenService

    override fun getIdentityProviderLoginDestination(target: String?): String {
        val oidp = properties.singpass.openIdProvider
        val uriComponentsBuilder = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(oidp.host)
            .path(oidp.authorizeEndpoint)
            .queryParam("response_type", "code")
            .queryParam("scope", "openid")
            .queryParam("client_id", oidp.clientId)
            .queryParam("redirect_uri", oidp.redirectUri)
            .queryParam("state", this.computeConsumerState(target))
            .queryParam("nonce", UUID.randomUUID())
        val uriComponents: UriComponents = uriComponentsBuilder
            .build()
            .encode()
        return uriComponents.toUriString()
    }

    override fun getToken(authCode: String): String {
        val oidp = properties.singpass.openIdProvider
        val tokenResponse = TokenResolver.resolveToken(authCode, oidp)
        val user = openidTokenService.parse(tokenResponse)

        return tokenService.build(user)
    }

    override fun refreshToken(token: String): String {
        var user = tokenService.parse(token)
        val tokenIssuedAt = user.iat
        val oidp = properties.singpass.openIdProvider
        user.refreshToken?.let {
            val tokenResponse = TokenResolver.refreshToken(it, oidp)
            val refreshedUser = openidTokenService.parse(tokenResponse)
            user = User(
                userName = refreshedUser.userName,
                refreshToken = refreshedUser.refreshToken,
                iat = tokenIssuedAt
            )
        }
        return tokenService.build(user)
    }
}
