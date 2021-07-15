package auth.service

import auth.model.openid.TokenResponse
import auth.util.helper.OpenIdProvider
import auth.util.jwt.TokenParser
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType.PERMIT
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jwk.JsonWebKey.OutputControlLevel
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.AlgorithmIdentifiers
import org.springframework.web.util.UriComponentsBuilder

abstract class BaseOpenIdTokenService<T> {
    object AlgorithmContraint {
        internal val signingAlgConstraints = AlgorithmConstraints(
            PERMIT,
            AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256,
            AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384,
            AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512
        )
        internal val encryptionAlgConstraints = AlgorithmConstraints(
            PERMIT,
            KeyManagementAlgorithmIdentifiers.ECDH_ES_A128KW,
            KeyManagementAlgorithmIdentifiers.ECDH_ES_A192KW,
            KeyManagementAlgorithmIdentifiers.ECDH_ES_A256KW,
            KeyManagementAlgorithmIdentifiers.RSA_OAEP_256,
        )
    }

    internal abstract val openIdProvider: OpenIdProvider?

    internal val signingJwk: PublicJsonWebKey? by lazy {
        openIdProvider?.token?.signingJwk?.let {
            val jwk = PublicJsonWebKey.Factory.newPublicJwk(it)
            AlgorithmContraint.signingAlgConstraints.checkConstraint(jwk.algorithm)
            jwk
        }
    }

    internal val httpsJwks: HttpsJwks? by lazy {
        openIdProvider?.token?.jwksEndpoint?.let {
            var jwksUrl = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(openIdProvider!!.host)
                .path(it)
                .build()
                .encode()
                .toUriString()
            HttpsJwks(jwksUrl)
        }
    }

    internal val encryptionJwk: PublicJsonWebKey? by lazy {
        openIdProvider?.token?.encryptionJwk?.let {
            val jwk = PublicJsonWebKey.Factory.newPublicJwk(it)
            AlgorithmContraint.encryptionAlgConstraints.checkConstraint(jwk.algorithm)
            jwk
        }
    }

    internal val tokenParser: TokenParser
        get() {
            val tokenParser = TokenParser()
            this.openIdProvider?.let {
                val issuer = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host(it.host)
                    .build()
                    .encode()
                    .toUriString()

                tokenParser.setIssuer(issuer).setAudience(it.clientId)
            }
            this.httpsJwks?.let {
                tokenParser.setVerificationHttpsJwks(it)
            }
            this.encryptionJwk?.let {
                tokenParser.setDecryptionKey(it.privateKey)
            }
            return tokenParser
        }

    abstract fun parse(tokenResponse: TokenResponse): T

    fun getJsonWebKeys(): List<Map<String, Any>> {
        val jwks: MutableList<Map<String, Any>> = mutableListOf()
        this.encryptionJwk?.let {
            jwks.add(it.toParams(OutputControlLevel.PUBLIC_ONLY))
        }
        this.signingJwk?.let {
            jwks.add(it.toParams(OutputControlLevel.PUBLIC_ONLY))
        }
        return jwks.toList()
    }
}
