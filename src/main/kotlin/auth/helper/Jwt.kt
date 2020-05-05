package auth.helper

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType.WHITELIST
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import java.security.Key
import java.nio.file.Files
import java.nio.file.Paths
import org.slf4j.LoggerFactory

class AdditionalInfoRequestException(message: String) : Exception(message)

const val MILLISECONDS_IN_MINUTE = 60_000

@Service
class Jwt(restTemplateBuilder: RestTemplateBuilder) {
    private val logger = LoggerFactory.getLogger(Jwt::class.java)
    @Autowired
    private lateinit var properties: Properties
    private val signingAlgConstraints = AlgorithmConstraints(
        WHITELIST,
        AlgorithmIdentifiers.RSA_USING_SHA256,
        AlgorithmIdentifiers.RSA_USING_SHA384,
        AlgorithmIdentifiers.RSA_USING_SHA512
    )
    private val encryptionAlgConstraints = AlgorithmConstraints(
        WHITELIST,
        ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
        ContentEncryptionAlgorithmIdentifiers.AES_192_CBC_HMAC_SHA_384,
        ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512
    )
    private val token by lazy {
        properties.token!!
    }
    private val signingKey: Key by lazy {
        signingAlgConstraints.checkConstraint(this.token.signatureAlgorithm)
        Cryptography.generatePrivate(this.token.privateKey)
    }
    private val verificationKey: Key by lazy {
        signingAlgConstraints.checkConstraint(this.token.signatureAlgorithm)
        val publicKey = String(Files.readAllBytes(Paths.get(this.token.publicKeyPath)))
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
        Cryptography.generatePublic(publicKey)
    }
    private val encryptionJwk: JsonWebKey? by lazy {
        var jwk: JsonWebKey? = null
        if (this.token.encryptionJwk != null) {
            jwk = JsonWebKey.Factory.newJwk(this.token.encryptionJwk)
            encryptionAlgConstraints.checkConstraint(jwk.getAlgorithm())
        }
        jwk
    }
    private val restTemplate = restTemplateBuilder.build()

    private fun create(issuer: String, claims: Map<String, Any>): String {
        val jwtClaims = JwtClaims()
        jwtClaims.setIssuer(issuer)
        jwtClaims.setExpirationTimeMinutesInTheFuture((this.token.expirationTime / MILLISECONDS_IN_MINUTE).toFloat())
        claims.forEach { key, value -> jwtClaims.setClaim(key, value) }

        val mutatedJwtClaims = this.token.plugin.instance.mutate(jwtClaims)

        val jws = JsonWebSignature()
        jws.setPayload(mutatedJwtClaims.toJson())
        jws.setAlgorithmHeaderValue(this.token.signatureAlgorithm)
        jws.setKey(this.signingKey)

        if (this.encryptionJwk == null) {
            return jws.getCompactSerialization()
        } else {
            val jwe = JsonWebEncryption()
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT)
            jwe.setEncryptionMethodHeaderParameter(encryptionJwk!!.getAlgorithm())
            jwe.setKey(encryptionJwk!!.getKey())
            jwe.setContentTypeHeaderValue("JWT")
            jwe.setPayload(jws.getCompactSerialization())
            return jwe.getCompactSerialization()
        }
    }

    fun build(issuer: String, claims: Map<String, Any>): String {
        val token = this.create(issuer, claims)

        val additionalInfoRequest: AdditionalInfoRequest? = when (issuer) {
            "Singpass" -> properties.singpass!!.additionalInfoRequest
            "Corppass" -> properties.corppass!!.additionalInfoRequest
            else -> null
        }
        if (additionalInfoRequest != null) {
            if (additionalInfoRequest.url != null) {
                try {
                    val headers = HttpHeaders()
                    headers.add("Authorization", "Bearer $token")
                    headers.setContentType(MediaType.APPLICATION_JSON)
                    val request = HttpEntity<String>(additionalInfoRequest.body, headers)
                    val respType = object : ParameterizedTypeReference<Map<String, Any>>() {}
                    val response = this.restTemplate.exchange(
                        additionalInfoRequest.url,
                        HttpMethod.valueOf(additionalInfoRequest.httpMethod),
                        request,
                        respType
                    )

                    val claimsWithAdditionalInfo = claims.plus(mapOf("additionalInfo" to response.getBody()))

                    return this.create(issuer, claimsWithAdditionalInfo)
                } catch (e: Exception) {
                    logger.error("Error in additional info request", e)
                    throw AdditionalInfoRequestException("Error while fetching additional info")
                }
            } else if (additionalInfoRequest.static != null) {
                val claimsWithAdditionalInfo = claims.plus(mapOf("additionalInfo" to additionalInfoRequest.static!!))
                return this.create(issuer, claimsWithAdditionalInfo)
            }
        }
        return token
    }

    fun buildSingpass(claims: Map<String, Any>): String {
        return this.build("Singpass", claims)
    }

    fun buildCorppass(claims: Map<String, Any>): String {
        return this.build("Corppass", claims)
    }

    fun parse(issuer: String, jwt: String): Map<String, Any> {
        val jwtConsumerBuilder = JwtConsumerBuilder()
            .setRequireExpirationTime()
            .setExpectedIssuer(issuer)
            .setJwsAlgorithmConstraints(AlgorithmConstraints(WHITELIST, this.token.signatureAlgorithm))
            .setVerificationKey(this.verificationKey)

        if (this.encryptionJwk != null) {
            jwtConsumerBuilder
                .setJweContentEncryptionAlgorithmConstraints(
                    AlgorithmConstraints(WHITELIST, this.encryptionJwk!!.getAlgorithm())
                )
                .setDecryptionKey(this.encryptionJwk!!.getKey())
        }

        return jwtConsumerBuilder.build().processToClaims(jwt).getClaimsMap()
    }

    fun parseSingpass(jwt: String): Map<String, Any> {
        return this.parse("Singpass", jwt)
    }

    fun parseCorppass(jwt: String): Map<String, Any> {
        return this.parse("Corppass", jwt)
    }
}
