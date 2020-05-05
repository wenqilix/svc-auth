package auth.helper

import java.util.Base64
import java.security.spec.X509EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.KeyFactory
import java.security.PublicKey
import java.security.PrivateKey

object Cryptography {
    fun generatePublic(keyString: String): PublicKey {
        val decodedKey = Base64.getDecoder().decode(keyString.toByteArray())
        val spec = X509EncodedKeySpec(decodedKey)
        val fact = KeyFactory.getInstance("RSA")
        return fact.generatePublic(spec)
    }

    fun generatePrivate(keyString: String): PrivateKey {
        val decodedKey = Base64.getDecoder().decode(keyString.toByteArray())
        val spec = PKCS8EncodedKeySpec(decodedKey)
        val fact = KeyFactory.getInstance("RSA")
        return fact.generatePrivate(spec)
    }
}
