package auth.service

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import org.junit.Assert.assertEquals
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.http.HttpStatus
import auth.helper.Properties
import auth.helper.Services
import auth.helper.SignatureAuthenticator
import auth.helper.Jwt
import auth.service.model.Service

@RunWith(MockitoJUnitRunner::class)
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

    @Mock
    lateinit var mockJwt: Jwt

    @Mock
    lateinit var mockProperties: Properties

    @Mock
    lateinit var mockSignatureAuthenticator: SignatureAuthenticator

    @InjectMocks
    lateinit var apiController: ApiController

    @Before
    fun setupMock() {
        mockServiceProps.servicesFolderPath = "src/test/resources/serviceTest"
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun requestTokenSuccessfully() {
        Mockito.`when`(mockSignatureAuthenticator.verifyNonce(nonce)).thenReturn(true)
        Mockito.`when`(mockSignatureAuthenticator.verifyService(nonce, mockSignature, mockService)).thenReturn(true)

        val result = apiController.token(username, mockSignature, nonce, "MCF")
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    fun requestTokenWithUnathorizedNonce() {
        Mockito.`when`(mockSignatureAuthenticator.verifyNonce(nonce)).thenReturn(false)
        Mockito.`when`(mockSignatureAuthenticator.verifyService(nonce, mockSignature, mockService)).thenReturn(true)

        val result = apiController.token(username, mockSignature, nonce, "MCF")
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    @Test
    fun requestTokenWithUnathorizedSignature() {
        Mockito.`when`(mockProperties.service).thenReturn(mockServiceProps)
        Mockito.`when`(mockSignatureAuthenticator.verifyNonce(nonce)).thenReturn(true)
        Mockito.`when`(mockSignatureAuthenticator.verifyService(nonce, mockSignature, mockService)).thenReturn(false)

        val result = apiController.token(username, mockSignature, nonce, "MCF")
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    @Test
    fun requestTokenWithBadRequest() {
        val result = apiController.token("nonexistent.user", mockSignature, nonce, "MCF")
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }
}