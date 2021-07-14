package auth.util.jwt

import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType.PERMIT
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver
import java.security.Key

class TokenParser {
    private val jwtConsumerBuilder = JwtConsumerBuilder()

    fun setVerificationKey(value: Key): TokenParser {
        jwtConsumerBuilder.setVerificationKey(value)
        return this
    }

    fun setVerificationAlgorithm(value: String): TokenParser {
        jwtConsumerBuilder.setJwsAlgorithmConstraints(
            AlgorithmConstraints(PERMIT, value)
        )
        return this
    }

    fun setVerificationHttpsJwks(value: HttpsJwks): TokenParser {
        val resolver = HttpsJwksVerificationKeyResolver(value)
        jwtConsumerBuilder.setVerificationKeyResolver(resolver)
        return this
    }

    fun setDecryptionKey(value: Key): TokenParser {
        jwtConsumerBuilder.setDecryptionKey(value)
        return this
    }

    fun setDecryptionAlgorithm(value: String): TokenParser {
        jwtConsumerBuilder.setJweContentEncryptionAlgorithmConstraints(
            AlgorithmConstraints(PERMIT, value)
        )
        return this
    }

    fun setIssuer(value: String): TokenParser {
        jwtConsumerBuilder.setExpectedIssuer(value)
        return this
    }

    fun setMaxAge(value: Long): TokenParser {
        jwtConsumerBuilder.setIssuedAtRestrictions(0, (value / 1000).toInt())
        return this
    }

    fun parse(jwt: String): Map<String, Any> {
        return jwtConsumerBuilder
            .setRequireExpirationTime()
            .build()
            .processToClaims(jwt)
            .getClaimsMap()
    }
}
