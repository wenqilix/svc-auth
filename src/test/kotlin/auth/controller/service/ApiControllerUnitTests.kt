package auth.controller.service

import auth.model.service.Service
import auth.service.TokenService
import auth.util.helper.Properties
import auth.util.helper.Services
import auth.util.helper.SignatureAuthenticator
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.http.HttpStatus

@TestInstance(Lifecycle.PER_CLASS)
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
    lateinit var mockTokenService: TokenService

    @MockK
    lateinit var mockProperties: Properties

    @MockK
    lateinit var mockSignatureAuthenticator: SignatureAuthenticator

    @InjectMockKs
    lateinit var apiController: ApiController

    @BeforeAll
    fun setupMock() {
        mockServiceProps.servicesFolderPath = "src/test/resources/serviceTest"
        MockKAnnotations.init(this)
        every { mockProperties.service } returns mockServiceProps
    }

    @Test
    fun requestTokenSuccessfully() {
        every { mockSignatureAuthenticator.verifyNonce(nonce) } returns true
        every { mockSignatureAuthenticator.verifyService(nonce, mockSignature, mockService) } returns true

        val result = apiController.token(username, mockSignature, nonce, "MCF")
        assertEquals(HttpStatus.OK, result.statusCode)
        verify { mockTokenService.invoke("build").withArguments(listOf("MCF", mockService.payload)) }
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
