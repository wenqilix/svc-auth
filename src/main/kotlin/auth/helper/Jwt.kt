package auth.helper

import org.springframework.stereotype.Service
import org.springframework.context.annotation.Profile
import org.springframework.beans.factory.annotation.Autowired
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import javax.crypto.spec.SecretKeySpec
import java.util.Date
import java.security.Key
import java.nio.file.Files
import java.nio.file.Paths

@Service
class Jwt {
    @Autowired
    lateinit internal var properties: Properties
    internal val token by lazy {
        properties.token!!
    }
    internal val signingKeyResolver by lazy {
        JwtSigningKeyResolver(this.verificationKey, this.token.signatureAlgorithm)
    }
    internal val signingKey: Key by lazy {
        val algo = SignatureAlgorithm.forName(this.token.signatureAlgorithm)
        if (algo.getFamilyName() != "RSA") {
            throw SignatureException("${algo.getValue()} Signature Algorithm Not Supported")
        }
        Cryptography.generatePrivate(this.token.privateKey)
    }
    internal val verificationKey: Key by lazy {
        val algo = SignatureAlgorithm.forName(this.token.signatureAlgorithm)
        if (algo.getFamilyName() != "RSA") {
            throw SignatureException("${algo.getValue()} Signature Algorithm Not Supported")
        }
        val publicKey = String(Files.readAllBytes(Paths.get("/app/shared/certs/public.pem")))
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
        Cryptography.generatePublic(publicKey)
    }

    fun build(claims: Map<String, Any>): String {
        return Jwts.builder()
            .addClaims(claims)
            .setExpiration(Date(System.currentTimeMillis() + this.token.expirationTime))
            .signWith(SignatureAlgorithm.forName(this.token.signatureAlgorithm), this.signingKey)
            .compact()
    }

    fun parse(jwt: String): Map<String, Any> {
        return Jwts.parser()
            .setSigningKeyResolver(signingKeyResolver)
            .parseClaimsJws(jwt)
            .getBody()
    }
}
