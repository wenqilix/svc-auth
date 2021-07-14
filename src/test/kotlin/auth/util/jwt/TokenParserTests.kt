package auth.util.jwt

import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class TokenParserTests {
    val mockClaims = mapOf("foo" to "bar", "iss" to "issuer")
    val jwk = RsaJwkGenerator.generateJwk(2048)

    val tokenParser = TokenParser()
        .setVerificationKey(jwk.publicKey)

    @Test
    fun `parse token successfully`() {
        val jwtClaims = JwtClaims()
        jwtClaims.setExpirationTimeMinutesInTheFuture(5.toFloat())
        mockClaims.forEach { key, value -> jwtClaims.setClaim(key, value) }

        val token = buildToken(jwtClaims)

        assertEquals(
            mockClaims,
            tokenParser.parse(token).filterKeys { key -> key != "exp" }
        )
    }

    @Test
    fun `parse failed when issuedAt exceed maxAge`() {
        val maxAge: Long = 60 * 1000
        val issuedAtExceedMaxAge = (System.currentTimeMillis() - (maxAge * 2)) / 1000
        val mockClaimWithExistingIssuedAt = mockClaims.plus(mapOf("iat" to issuedAtExceedMaxAge))
        val jwtClaims = JwtClaims()
        jwtClaims.setExpirationTimeMinutesInTheFuture(5.toFloat())
        mockClaimWithExistingIssuedAt.forEach { key, value -> jwtClaims.setClaim(key, value) }

        val token = buildToken(jwtClaims)
        try {
            tokenParser.setMaxAge(maxAge).parse(token)
            fail("should throw InvalidJwtException")
        } catch (e: Exception) {
            assertTrue(e is InvalidJwtException)
            assertTrue(e.message!!.contains("is more than 60 second(s) in the past."))
        }
    }

    @Test
    fun `parse failed when expected issuer is wrong`() {
        val jwtClaims = JwtClaims()
        jwtClaims.setExpirationTimeMinutesInTheFuture(5.toFloat())
        mockClaims.forEach { key, value -> jwtClaims.setClaim(key, value) }

        val token = buildToken(jwtClaims)
        try {
            tokenParser.setIssuer("wrong").parse(token)
            fail("should throw InvalidJwtException")
        } catch (e: Exception) {
            assertTrue(e is InvalidJwtException)
            assertTrue(e.message!!.contains("Issuer (iss) claim value (issuer) doesn't match expected value of wrong"))
        }
    }

    @Test
    fun `parse failed when no expiration`() {
        val jwtClaims = JwtClaims()
        mockClaims.forEach { key, value -> jwtClaims.setClaim(key, value) }

        val token = buildToken(jwtClaims)
        try {
            tokenParser.parse(token)
            fail("should throw InvalidJwtException")
        } catch (e: Exception) {
            assertTrue(e is InvalidJwtException)
            assertTrue(e.message!!.contains("No Expiration Time (exp) claim present."))
        }
    }

    @Test
    fun `parse failed when token expired`() {
        val jwtClaims = JwtClaims()
        jwtClaims.setExpirationTimeMinutesInTheFuture(-1.toFloat())
        mockClaims.forEach { key, value -> jwtClaims.setClaim(key, value) }

        val token = buildToken(jwtClaims)
        try {
            tokenParser.parse(token)
            fail("should throw InvalidJwtException")
        } catch (e: Exception) {
            assertTrue(e is InvalidJwtException)
            assertTrue(e.message!!.contains("is on or after the Expiration Time"))
        }
    }

    fun buildToken(claims: JwtClaims): String {
        val jws = JsonWebSignature()
        jws.setPayload(claims.toJson())
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512)
        jws.setKey(jwk.privateKey)

        return jws.getCompactSerialization()
    }
}
