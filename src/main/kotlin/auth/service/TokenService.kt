package auth.service

import auth.util.helper.Properties
import auth.util.jwt.TokenBuilder
import auth.util.jwt.TokenParser
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType.PERMIT
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jwk.JsonWebKey.OutputControlLevel
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.AlgorithmIdentifiers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Primary
@Service("TokenService")
open class TokenService {
    object AlgorithmContraint {
        internal val signingAlgConstraints = AlgorithmConstraints(
            PERMIT,
            AlgorithmIdentifiers.RSA_USING_SHA256,
            AlgorithmIdentifiers.RSA_USING_SHA384,
            AlgorithmIdentifiers.RSA_USING_SHA512
        )
    }

    @Autowired
    internal lateinit var properties: Properties

    internal val signingJwk: PublicJsonWebKey by lazy {
        val jwk = PublicJsonWebKey.Factory.newPublicJwk(properties.token.signingJwk)
        AlgorithmContraint.signingAlgConstraints.checkConstraint(jwk.algorithm)
        jwk
    }

    internal val encryptionHttpsJwks: HttpsJwks? by lazy {
        properties.token.encryptionJwksUrl?.let {
            var jwksUrl = UriComponentsBuilder
                .fromHttpUrl(it)
                .build()
                .encode()
                .toUriString()
            HttpsJwks(jwksUrl)
        }
    }

    fun parse(issuer: String, jwt: String): Map<String, Any> {
        val tokenParser = TokenParser()
            .setIssuer(issuer)
            .setVerificationKey(this.signingJwk.publicKey)
            .setVerificationAlgorithm(this.signingJwk.algorithm)
            .setMaxAge(properties.token.maxAge)
        return tokenParser.parse(jwt)
    }

    fun build(issuer: String, payload: Map<String, Any>): String {
        val tokenBuilder = TokenBuilder(properties.token.plugin)
            .setIssuer(issuer)
            .setSigningKey(this.signingJwk.privateKey)
            .setSigningAlgorithm(this.signingJwk.algorithm)
            .setExpiration(properties.token.expirationTime)
        this.encryptionHttpsJwks?.let { httpsJwks ->
            val encryptionJwk = httpsJwks.getJsonWebKeys().find {
                it.use == "enc"
            }
            encryptionJwk?.let {
                tokenBuilder
                    .setEncryptionMethod(ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512)
                    .setEncryptionAlgorithm(it.algorithm)
                    .setEncryptionKey(it.key)
            }
        }
        return tokenBuilder.build(payload)
    }

    fun getJsonWebKeys(): List<Map<String, Any>> {
        val jwks: MutableList<Map<String, Any>> = mutableListOf()
        jwks.add(this.signingJwk.toParams(OutputControlLevel.PUBLIC_ONLY))
        return jwks.toList()
    }
}
