package auth.util.jwt

import auth.util.helper.Plugin
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims

const val MILLISECONDS_IN_MINUTE = 60_000

class TokenBuilder(val plugin: Plugin?) {
    private val jwtClaims = JwtClaims()
    private val jws = JsonWebSignature()
    private val jwe = JsonWebEncryption()

    fun setSigningJwk(jwk: PublicJsonWebKey): TokenBuilder {
        jws.setKey(jwk.privateKey)
        jws.setAlgorithmHeaderValue(jwk.algorithm)
        jwk.keyId?.let {
            jws.setKeyIdHeaderValue(jwk.keyId)
        }
        return this
    }

    fun setSigningHeader(name: String, value: String): TokenBuilder {
        jws.setHeader(name, value)
        return this
    }

    fun setEncryptionJwk(jwk: JsonWebKey): TokenBuilder {
        jwe.setKey(jwk.key)
        jwe.setAlgorithmHeaderValue(jwk.algorithm)
        return this
    }

    fun setEncryptionMethod(value: String): TokenBuilder {
        jwe.setEncryptionMethodHeaderParameter(value)
        return this
    }

    fun setIssuer(value: String): TokenBuilder {
        jwtClaims.setIssuer(value)
        return this
    }

    fun setExpiration(value: Long): TokenBuilder {
        jwtClaims.setExpirationTimeMinutesInTheFuture((value / MILLISECONDS_IN_MINUTE).toFloat())
        return this
    }

    fun setSubject(value: String): TokenBuilder {
        jwtClaims.setSubject(value)
        return this
    }

    fun setAudience(vararg values: String): TokenBuilder {
        jwtClaims.setAudience(*values)
        return this
    }

    fun build(claims: Map<String, Any>): String {
        jwtClaims.setIssuedAtToNow()
        claims.forEach { key, value -> jwtClaims.setClaim(key, value) }

        val mutatedJwtClaims = plugin?.instance?.mutate(jwtClaims) ?: jwtClaims

        jws.setPayload(mutatedJwtClaims.toJson())

        if (jwe.key != null &&
            jwe.encryptionMethodHeaderParameter != null &&
            jwe.algorithmHeaderValue != null
        ) {
            jwe.setContentTypeHeaderValue("JWT")
            jwe.setPayload(jws.getCompactSerialization())
            return jwe.getCompactSerialization()
        } else {
            return jws.getCompactSerialization()
        }
    }
}
