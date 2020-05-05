package auth.helper

import java.util.Base64
import org.springframework.beans.factory.annotation.Autowired
import java.security.Signature
import org.springframework.stereotype.Service
import auth.service.model.Service as ServiceModel

/**
 * This class provide methods to verify signature provided in credentials to service login
 */
@Service
class SignatureAuthenticator {
    @Autowired
    internal lateinit var properties: Properties

    internal val servicesConfig by lazy {
        properties.service!!
    }

    companion object {
        const val SIGNATURE_ALGORITHM = "SHA256withRSA"
    }

    /**
     * Verify [signature] to be equal to [service guid] + [nonce]
     * @return result of signature verification
     */
    fun verifyService(nonce: String, signature: String, service: ServiceModel): Boolean {
        val key = Cryptography.generatePublic(service.publicKey)
        val verifier = Signature.getInstance(SIGNATURE_ALGORITHM)
        val signaturePayload = service.guid + nonce

        verifier.initVerify(key)
        verifier.update(signaturePayload.toByteArray())

        return verifier.verify(Base64.getDecoder().decode((signature.toByteArray())))
    }

    /**
     * Verify [nonce] to be issued within [signatureLifetimeClockSkew] time
     * @return result of nonce verification
     */
    fun verifyNonce(nonce: String): Boolean {
        val requestingTimestamp = nonce.toLong()
        val validatingTimestamp = System.currentTimeMillis()
        val durationDifference = validatingTimestamp - requestingTimestamp

        return durationDifference > 0 && durationDifference < servicesConfig.signatureLifetimeClockSkew
    }
}
