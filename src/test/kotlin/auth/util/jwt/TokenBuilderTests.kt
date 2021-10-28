package auth.util.jwt

import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class TokenBuilderTests {
    val mockClaims = mapOf("foo" to "bar")
    val signingJwk = RsaJwkGenerator.generateJwk(4096).let {
        it.algorithm = AlgorithmIdentifiers.RSA_USING_SHA512
        it
    }

    val tokenBuilder = TokenBuilder(null)
        .setSigningJwk(signingJwk)

    @Test
    fun `build token successfully`() {
        val result = tokenBuilder.build(mockClaims)

        val parsedToken = parseToken(result)
        assertNotNull(parsedToken.get("iat"))
        assertEquals(
            mockClaims,
            parsedToken.filterKeys { key -> key != "iat" }
        )
    }

    @Test
    fun `build token with existing issuedAt when issuedAt is provided`() {
        val issuedAtBeforeMaxAge = (System.currentTimeMillis()) / 1000
        val mockClaimWithExistingIssuedAt = mockClaims.plus(mapOf("iat" to issuedAtBeforeMaxAge))

        val result = tokenBuilder.build(mockClaimWithExistingIssuedAt)

        assertEquals(
            mockClaimWithExistingIssuedAt,
            parseToken(result)
        )
    }

    @Test
    fun `build token with expiration setted`() {
        val result = tokenBuilder
            .setExpiration(1 * 60 * 1000L)
            .build(mockClaims)

        val parsedToken = parseToken(result)
        assertNotNull(parsedToken.get("exp"))
        assertEquals(
            mockClaims,
            parsedToken.filterKeys { key -> key != "exp" && key != "iat" }
        )
    }

    @Test
    fun `build token with issuer setted`() {
        val result = tokenBuilder
            .setIssuer("whatever")
            .build(mockClaims)

        val parsedToken = parseToken(result)
        assertEquals(
            mockClaims.plus(mapOf("iss" to "whatever")),
            parsedToken.filterKeys { key -> key != "iat" }
        )
    }

    @Test
    fun `build token with subject setted`() {
        val result = tokenBuilder
            .setSubject("some subject")
            .build(mockClaims)

        val parsedToken = parseToken(result)
        assertEquals(
            mockClaims.plus(mapOf("sub" to "some subject")),
            parsedToken.filterKeys { key -> key != "iat" }
        )
    }

    @Test
    fun `build token with audience setted`() {
        val result = tokenBuilder
            .setAudience("audience")
            .build(mockClaims)

        val parsedToken = parseToken(result)
        assertEquals(
            mockClaims.plus(mapOf("aud" to "audience")),
            parsedToken.filterKeys { key -> key != "iat" }
        )
    }

    @Test
    fun `build token with multiple audience setted`() {
        val result = tokenBuilder
            .setAudience("audience 1", "audience 2")
            .build(mockClaims)

        val parsedToken = parseToken(result)
        assertEquals(
            mockClaims.plus(mapOf("aud" to listOf("audience 1", "audience 2"))),
            parsedToken.filterKeys { key -> key != "iat" }
        )
    }

    @Test
    fun `build token with encryption`() {
        val encryptionJwk = RsaJwkGenerator.generateJwk(4096)
        encryptionJwk.algorithm = KeyManagementAlgorithmIdentifiers.RSA_OAEP_256

        val result = tokenBuilder
            .setEncryptionMethod(ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512)
            .setEncryptionJwk(encryptionJwk)
            .build(mockClaims)

        val tokenBreakdown = result.split(".")
        assertEquals(tokenBreakdown.size, 5) // jwe contains 5 parts

        val parsedToken = JwtConsumerBuilder()
            .setSkipSignatureVerification()
            .setDecryptionKey(encryptionJwk.privateKey)
            .build()
            .processToClaims(result)
            .getClaimsMap()
        assertNotNull(parsedToken.get("iat"))
        assertEquals(
            mockClaims,
            parsedToken.filterKeys { key -> key != "iat" }
        )
    }

    fun parseToken(token: String): Map<String, Any> {
        val jwtConsumerBuilder = JwtConsumerBuilder()
            .setSkipSignatureVerification()
            .setSkipDefaultAudienceValidation()
        return jwtConsumerBuilder.build().processToClaims(token).getClaimsMap()
    }
}
