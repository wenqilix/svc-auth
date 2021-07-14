package auth.util.helper

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@TestInstance(Lifecycle.PER_CLASS)
class AdditionalInfoRequestTests {
    val mockAdditonalInfo = mapOf("addition" to "info")

    @RelaxedMockK
    lateinit var restTemplate: RestTemplate

    @BeforeAll
    fun setupMock() {
        MockKAnnotations.init(this)
        val respType = object : ParameterizedTypeReference<Map<String, Any>>() {}
        every {
            restTemplate.exchange("http://localhost", HttpMethod.GET, any(), respType)
        } returns ResponseEntity(mockAdditonalInfo, HttpStatus.OK)
        every {
            restTemplate.exchange("http://localhost/error", HttpMethod.GET, any(), respType)
        } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)
        every {
            restTemplate.exchange("http://localhost/gql-error", HttpMethod.GET, any(), respType)
        } returns ResponseEntity(mapOf("errors" to listOf("some error")), HttpStatus.OK)
    }

    @Test
    fun `return null no additional info configured`() {
        val additionalInfoRequest = AdditionalInfoRequest(restTemplate)
        additionalInfoRequest.url = null
        additionalInfoRequest.staticJson = null
        val result = additionalInfoRequest.resolveAdditionalInfo()

        assertEquals(
            null,
            result,
            "Should return null when additionalInfoRequest not configured"
        )
    }

    @Test
    fun `return fetch and response body when additionalInfoRequest url exists`() {
        val additionalInfoRequest = AdditionalInfoRequest(restTemplate)
        additionalInfoRequest.url = "http://localhost"

        val result = additionalInfoRequest.resolveAdditionalInfo()

        assertEquals(
            mockAdditonalInfo,
            result,
            "Should return additionalInfo response when additionalInfoRequest url exists"
        )
    }

    @Test
    fun `return additionalInfo static payload when additionalInfoRequest is of staticJson`() {
        val additionalInfoRequest = AdditionalInfoRequest(restTemplate)
        additionalInfoRequest.url = null
        additionalInfoRequest.staticJson = "{\"static\":\"addition\"}"

        val result = additionalInfoRequest.resolveAdditionalInfo()

        assertEquals(
            mapOf("static" to "addition"),
            result,
            "Should return additionalInfo static payload when additionalInfoRequest is of staticJson"
        )
    }

    @Test
    fun `return static payload when additionalInfoRequest has error`() {
        val additionalInfoRequest = AdditionalInfoRequest(restTemplate)
        additionalInfoRequest.url = "http://localhost/error"
        additionalInfoRequest.staticJson = "{\"static\":\"addition\"}"

        val result = additionalInfoRequest.resolveAdditionalInfo()

        assertEquals(
            mapOf("static" to "addition"),
            result,
            "Should return static payload when additionalInfoRequest has error"
        )
    }

    @Test
    fun `return static payload when additionalInfoRequest has graphql like error`() {
        val additionalInfoRequest = AdditionalInfoRequest(restTemplate)
        additionalInfoRequest.url = "http://localhost/gql-error"
        additionalInfoRequest.staticJson = "{\"static\":\"addition\"}"

        val result = additionalInfoRequest.resolveAdditionalInfo()

        assertEquals(
            mapOf("static" to "addition"),
            result,
            "Should return static payload when additionalInfoRequest is graphql and has error"
        )
    }
}
