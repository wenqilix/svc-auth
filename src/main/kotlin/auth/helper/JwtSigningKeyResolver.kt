package auth.helper

import io.jsonwebtoken.SigningKeyResolverAdapter
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Claims
import io.jsonwebtoken.SignatureException
import java.security.Key

class JwtSigningKeyResolver(val verificationKey: Key, val algorithm: String) : SigningKeyResolverAdapter() {
    override fun resolveSigningKey(header: JwsHeader<*>, claims: Claims): Key {
        if (header.getAlgorithm() != algorithm) {
            throw SignatureException("Invalid Signature Algorithm ${header.getAlgorithm()}")
        }
        return this.verificationKey
    }
}
