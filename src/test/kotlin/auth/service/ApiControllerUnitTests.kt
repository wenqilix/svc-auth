package auth.service

import org.junit.Test
import org.junit.Before
import org.junit.Assert.assertEquals
import org.springframework.http.HttpStatus
import auth.helper.Properties
import auth.helper.Services
import auth.helper.SignatureAuthenticator
import auth.helper.Jwt
import auth.service.model.Service
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.MockKAnnotations
import io.mockk.every

class ApiControllerUnitTests {
    val mockSignature = "PTET6SK33zYim5avg+FX3sepa6rH/IB0dNqXuc1l3Uu4L6+4vkAoExMTQZXhr5JEFrPOZVLajLpqeUjMYiToFw=="
    val username = "serviceMock"
    val nonce = "1538646696023"
    var mockServiceProps = Services()
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

    @RelaxedMockK
    lateinit var mockJwt: Jwt

    @MockK
    lateinit var mockProperties: Properties

    @MockK
    lateinit var mockSignatureAuthenticator: SignatureAuthenticator

    @InjectMockKs
    lateinit var apiController: ApiController

    @Before
    fun setupMock() {
        mockServiceProps.servicesFolderPath = "src/test/resources/serviceTest"
        MockKAnnotations.init(this)
    }

    @Test
    fun requestTokenSuccessfully() {
        every { mockSignatureAuthenticator.verifyNonce(nonce) } returns true
        every { mockSignatureAuthenticator.verifyService(nonce, mockSignature, mockService) } returns true

        val result = apiController.token(username, mockSignature, nonce, "MCF")
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    fun requestTokenWithUnathorizedNonce() {
        every { mockSignatureAuthenticator.verifyNonce(nonce) } returns false
        every { mockSignatureAuthenticator.verifyService(nonce, mockSignature, mockService) } returns true

        val result = apiController.token(username, mockSignature, nonce, "MCF")
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    @Test
    fun requestTokenWithUnathorizedSignature() {
        every { mockProperties.service } returns mockServiceProps
        every { mockSignatureAuthenticator.verifyNonce(nonce) } returns true
        every { mockSignatureAuthenticator.verifyService(nonce, mockSignature, mockService) } returns false

        val result = apiController.token(username, mockSignature, nonce, "MCF")
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    @Test
    fun requestTokenWithBadRequest() {
        val result = apiController.token("nonexistent.user", mockSignature, nonce, "MCF")
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }
}