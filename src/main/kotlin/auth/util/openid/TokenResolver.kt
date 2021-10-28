package auth.util.openid

import auth.model.openid.TokenResponse
import auth.util.helper.OpenIdProvider
import auth.util.jwt.TokenBuilder
import org.jose4j.jwk.PublicJsonWebKey
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.InetSocketAddress
import java.net.Proxy

object TokenResolver {
    private val logger = LoggerFactory.getLogger(TokenResolver::class.java)
    private fun getRestTemplate(openIdProvider: OpenIdProvider): RestTemplate {
        val requestFactory = openIdProvider.proxyHost?.let {
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(it, openIdProvider.proxyPort))
            SimpleClientHttpRequestFactory().let {
                it.setProxy(proxy)
                it
            }
        }
        return if (requestFactory != null) RestTemplate(requestFactory) else RestTemplate()
    }

    private fun generateClientAssertion(openIdProvider: OpenIdProvider): String {
        val audience = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(openIdProvider.host)
            .build()
            .encode()
            .toUriString()
        val signingJwk = PublicJsonWebKey.Factory.newPublicJwk(openIdProvider.token.signingJwk)
        val tokenBuilder = TokenBuilder(null)
            .setIssuer(openIdProvider.clientId)
            .setSubject(openIdProvider.clientId)
            .setSigningJwk(signingJwk)
            .setSigningHeader("typ", "JWT")
            .setExpiration(openIdProvider.clientAssertionJwtExpirationTime)
            .setAudience(audience)

        return tokenBuilder.build(emptyMap())
    }

    private fun getAuthorizationInfoToken(accessToken: String, openIdProvider: OpenIdProvider): String? {
        try {
            return openIdProvider.authorizationInfoEndpoint?.let {
                val authorizationInfoUrl = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host(openIdProvider.host)
                    .path(it)
                    .build()
                    .encode()
                    .toUriString()
                val httpHeaders = HttpHeaders().also { it.setBearerAuth(accessToken) }
                val authorizationInfoRequest = HttpEntity(null, httpHeaders)

                getRestTemplate(openIdProvider).postForObject(
                    authorizationInfoUrl,
                    authorizationInfoRequest,
                    String::class.java
                )
            }
        } catch (e: Exception) {
            logger.error("Error in fetching openid Authorization Info")
            when (e) {
                is HttpClientErrorException -> {
                    logger.error(e.responseBodyAsString)
                }
            }
            throw e
        }
    }

    fun resolveToken(code: String, openIdProvider: OpenIdProvider): TokenResponse {
        try {
            val tokenUrl = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(openIdProvider.host)
                .path(openIdProvider.tokenEndpoint)
                .build()
                .encode()
                .toUriString()

            val httpHeaders = HttpHeaders().also { it.contentType = MediaType.APPLICATION_FORM_URLENCODED }
            val tokenRequestParameters = LinkedMultiValueMap<String, String>().also {
                it.add("grant_type", "authorization_code")
                it.add("code", code)
                it.add("redirect_uri", openIdProvider.redirectUri)
                it.add("client_id", openIdProvider.clientId)
                it.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                it.add("client_assertion", generateClientAssertion(openIdProvider))
            }
            val tokenRequest = HttpEntity(tokenRequestParameters, httpHeaders)

            val tokenResponse = getRestTemplate(openIdProvider).postForObject(
                tokenUrl,
                tokenRequest,
                TokenResponse::class.java
            )

            tokenResponse.authorizationInfoToken = getAuthorizationInfoToken(tokenResponse.accessToken, openIdProvider)

            return tokenResponse
        } catch (e: Exception) {
            logger.error("Error in resolving openid token")
            when (e) {
                is HttpClientErrorException -> {
                    logger.error(e.responseBodyAsString)
                }
            }
            throw e
        }
    }

    fun refreshToken(refreshToken: String, openIdProvider: OpenIdProvider): TokenResponse {
        try {
            val refreshUrl = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(openIdProvider.host)
                .path(openIdProvider.tokenEndpoint)
                .build()
                .encode()
                .toUriString()

            val httpHeaders = HttpHeaders().also { it.contentType = MediaType.APPLICATION_FORM_URLENCODED }
            val tokenRequestParameters = LinkedMultiValueMap<String, String>().also {
                it.add("grant_type", "refresh_token")
                it.add("refresh_token", refreshToken)
                it.add("scope", "openid")
                it.add("client_id", openIdProvider.clientId)
                it.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                it.add("client_assertion", generateClientAssertion(openIdProvider))
            }
            val request = HttpEntity(tokenRequestParameters, httpHeaders)

            return getRestTemplate(openIdProvider).postForObject(
                refreshUrl,
                request,
                TokenResponse::class.java
            )
        } catch (e: Exception) {
            logger.error("Error in refreshing openid token")
            when (e) {
                is HttpClientErrorException -> {
                    logger.error(e.responseBodyAsString)
                }
            }
            throw e
        }
    }
}
