package auth.helper

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import auth.service.model.Service

@RunWith(MockitoJUnitRunner::class)
class SignatureAuthenticatorTests {
    var mockServiceProps = Services()
    val validSignature = "hCcSOrZF8l7wdS8a8Sd17PMGhAbViKGb8J1+HrbTZSC4aRijIuoB10/Bi6eT+9BGHbmPu1ET9iJpJyq2MHrVkg=="
    val invalidSignature = "XCcSOrZF8l7wdS8a8Sd17PMGhAbViKGb8J1+HrbTZSC4aRijIuoB10/Bi6eT+9BGHbmPu1ET9iJpJyq2MHrVkg=="
    val mockSignatureLifetimeClockSkew: Long = 10000
    val validNonce = "1538646696023"
    val invalidNonce = "1538646696024"
    val mockService = Service(
        mapOf(
            "guid" to "serviceMock",
            "publicKey" to "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKYeZ1pJWL7adv4bRdutKtxVYmdkWrbSbBgN/edB54Fk8G8RukHeuXC7dFIgtndB1TR4xHbfIEJ+Uo6RhvoKZvcCAwEAAQ==",
            "payload" to mapOf(
                "userId" to "api-tester",
                "authorization" to arrayListOf(mapOf("id" to "*", "scopes" to arrayListOf("foobar:read")))
            )
        )
    )

    @Mock
    lateinit var mockProperties: Properties

    @InjectMocks
    lateinit var signatureAuthenticator: SignatureAuthenticator

    @Before
    fun setupMock() {
        mockServiceProps.signatureLifetimeClockSkew = mockSignatureLifetimeClockSkew
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun verifyServicePassed() {
        val result = signatureAuthenticator.verifyService(validNonce, validSignature, mockService)

        assertTrue(result)
    }

    @Test
    fun verifyServiceFailedWithInvalidPayload() {
        val result = signatureAuthenticator.verifyService(invalidNonce, validSignature, mockService)

        assertFalse(result)
    }

    @Test
    fun verifyServiceFailedWithInvalidSignature() {
        val result = signatureAuthenticator.verifyService(validNonce, invalidSignature, mockService)

        assertFalse(result)
    }

    @Test
    fun verifyNoncePassed() {
        Mockito.`when`(mockProperties.service).thenReturn(mockServiceProps)
        val nonceToVerify = (System.currentTimeMillis() - 5000).toString()

        val result = signatureAuthenticator.verifyNonce(nonceToVerify)

        assertTrue(result)
    }

    @Test
    fun verifyNonceFailed() {
        Mockito.`when`(mockProperties.service).thenReturn(mockServiceProps)
        val nonceToVerify = (System.currentTimeMillis() - mockSignatureLifetimeClockSkew).toString()

        val result = signatureAuthenticator.verifyNonce(nonceToVerify)

        assertFalse(result)
    }

    @Test
    fun verifyNonceFailedWithFutureTime() {
        val nonceToVerify = (System.currentTimeMillis() + mockSignatureLifetimeClockSkew).toString()

        val result = signatureAuthenticator.verifyNonce(nonceToVerify)

        assertFalse(result)
    }
}