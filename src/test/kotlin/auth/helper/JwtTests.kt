package auth.helper

import org.junit.Test
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.OverrideMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.every
import io.mockk.MockKAnnotations

class JwtTests {
    val mockClaims = mapOf("foo" to "bar")
    val mockAdditonalInfo = mapOf("addition" to "info")

    @MockK
    lateinit var mockSingpassProperties: Provider
    @MockK
    lateinit var mockCorppassProperties: Provider
    @MockK
    lateinit var mockProperties: Properties
    @RelaxedMockK
    lateinit var restTemplateBuilder: RestTemplateBuilder
    @MockK
    lateinit var restTemplate: RestTemplate

    @OverrideMockKs
    lateinit var jwt: Jwt

    @Before
    fun setupMock() {
        MockKAnnotations.init(this)
        val respType = object : ParameterizedTypeReference<Map<String, Any>>() {}
        every {
            restTemplate.exchange("http://localhost", HttpMethod.GET, any(), respType)
        } returns ResponseEntity(mockAdditonalInfo, HttpStatus.OK)
        every {
            restTemplate.exchange("http://localhost/error", HttpMethod.GET, any(), respType)
        } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        every { mockProperties.singpass } returns mockSingpassProperties
        every { mockProperties.corppass } returns mockCorppassProperties

        val mockToken = Token()
        mockToken.privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDBZ1duiBouc4Wm5tFawSy1FYum7ZQEjGvN9P1M8F/aIWVPDHWOXZld/VsqyskJ7tq3i6TiP8F83CuNZvulxq01sXoNGD+yTIF3KKqiDPKNcuZkLa5MNUpZD01h7GtzLIpEKpoBbxYVkbYtSS8j3ouRW/oqM2xciu9Hm6kU1tWXPKcgqiR9VEpIeBx9OoxcxsZaJFTji0Tf2hT7FXCALRqjHyGxHewREfd1gm1tJB4/1GRTJNktZehSvBTVMgSEZEdBeCSYWVqcCQYcpwWaBE65ybg+tYKR62hOM732fmSinQEt7fRGXeQO4Y1Y8B8aGt3udty6otTbg6t/qPcRX3CFAgMBAAECggEAFEGOlrUKiPLwfJqQMsEovY6UIdegORm78MZIAVbrsxzsT2supMqI1Z9DfSfgBnpNzguCKXAkA7/Lj/PJR4OXgJgmAFkZ0sLQjHpl5LMWdFokZzmhl7m9H7bKlAb++gEgOiN8HcpUtc2dFwEjPs6AL6lCekkIgGjMxuM2wkf99ujkvAdXdJ8V93wwNbSiFlnuBTxbP5DMKD3J96qX7rpCaMfh6BQAEIP7ihshYEnSQXSFMxDNwiBHi+0HUQy1Ypv9VavBhUFFcZ5mBXtSzSo6Bz2TqaVwTIEmiuPXliOi2YMmOpNrVzjB7PT8A3KzsCVzemeqVUtwGICEAWG6+u+8AQKBgQDezmU73uN8zI8hVu1vNn94/6uiDIo9AtaBhR1dZkGT/7cz+huwCF//LmHQ7mflCvqoBJvc2wWsh9MLQbR8v5q2jQY8M908GVW9pdLIJQFpouR1WWT8Lq+XED2UlYAORxMEP8Z1ZlzJoHNhj5LaYJ65O90JmHqE8D+vHK/fcYCj8QKBgQDeN4/3sBlIw3AnPMvtmQAgoRibH1tMJqNrRqggeoJ1ODTOr6CpqkKG+BwBg7AxoAKphmeJo5rzIdz7U31KVIyzQO+NDdqZ7V90elv8sWR9zJ3Xz34aTks4B0Vva+m2idxY6Z4GiLpY4CY6Y/eOnRjzLIQqXs8yiC86+SL/4RyZ1QKBgCypcM+EiVNN6e8D7spZQUou7eeo3dkHBZqm5VEAm2qgWsf3fa9Wrm+Hi6IG+BqIjeA6NEgcO1HvVBWwkQ5klg27zSEqQFG4vmAqqkrmaBcVyPfg+IY4kYgVgFKVeTrjcmNqqUyFPVpJaHeJASX3MjntTwyKXRziz9E5TePba67xAoGAKUE5eit4Vjjqbm5sEb1Q973QuqPUqEuYFUd12SnFZIu9yg1zD2yrkzEmyeUv5damB+ELAdOc5byIsSQr44vU30aT8qT6y471JMO+pigs1uoQ98rux2V3s3wuGOR47Eml3d/pVZ82sV9T2Y1LnW9u/V5rXw5g+ymrUxCDveuGCs0CgYEAs3lKxDImcQG05aWJsW86YOU9GfSNdANtlLyEK6GKwhWakgFNNPudWr9gbZW5FQ5ietV+eJpRvcKDr3TYzhuZh923/MZe0Eia4PHce7Sumbbvr+o4dJo6Pfvw8nxtIFnLkDbA5Jsa/qqMUuF+38GKj+TY1IxcR7zVbr+Z0VhYOmE="
        mockToken.publicKeyPath = "src/main/resources/certs/public.pem"
        mockToken.signatureAlgorithm = "RS512"
        mockToken.expirationTime = 60_000
        every { mockProperties.token } returns mockToken
    }

    @Test
    fun buildSingpassWithoutAdditionalInfo() {
        every { mockSingpassProperties.additionalInfoRequest.url } returns null
        every { mockSingpassProperties.additionalInfoRequest.static } returns null
        val result = jwt.buildSingpass(mockClaims)

        assertEquals(
            mockClaims.plus(mapOf("iss" to "Singpass")),
            jwt.parseSingpass(result).filterKeys { key -> key != "exp" }
        )
    }

    @Test
    fun buildSingpassWithAdditionalInfoUrl() {
        val additionalInfoRequest = AdditionalInfoRequest()
        additionalInfoRequest.url = "http://localhost"
        additionalInfoRequest.httpMethod = "GET"

        every { mockSingpassProperties.additionalInfoRequest } returns additionalInfoRequest
        val result = jwt.buildSingpass(mockClaims)

        assertEquals(
            mockClaims.plus(mapOf("iss" to "Singpass", "additionalInfo" to mockAdditonalInfo)),
            jwt.parseSingpass(result).filterKeys { key -> key != "exp" }
        )
    }

    @Test
    fun buildSingpassWithAdditionalInfoStaticJson() {
        val additionalInfoRequest = AdditionalInfoRequest()
        additionalInfoRequest.url = null
        additionalInfoRequest.staticJson = "{\"static\":\"addition\"}"

        every { mockSingpassProperties.additionalInfoRequest } returns additionalInfoRequest
        val result = jwt.buildSingpass(mockClaims)

        assertEquals(
            mockClaims.plus(mapOf("iss" to "Singpass", "additionalInfo" to mapOf("static" to "addition"))),
            jwt.parseSingpass(result).filterKeys { key -> key != "exp" }
        )
    }

    @Test
    fun buildSingpassWithAdditionalInfoUrlError() {
        val additionalInfoRequest = AdditionalInfoRequest()
        additionalInfoRequest.url = "http://localhost/error"
        additionalInfoRequest.httpMethod = "GET"

        every { mockSingpassProperties.additionalInfoRequest } returns additionalInfoRequest
        try {
            jwt.buildSingpass(mockClaims)
            fail("Should be throwing AdditionalInfoRequestException when additionalInfoRequest has error")
        } catch (e: Exception) {
            assertTrue(e is AdditionalInfoRequestException)
        }
    }

    @Test
    fun buildCorppassWithoutAdditionalInfo() {
        every { mockCorppassProperties.additionalInfoRequest.url } returns null
        every { mockCorppassProperties.additionalInfoRequest.static } returns null
        val result = jwt.buildCorppass(mockClaims)

        assertEquals(
            mockClaims.plus(mapOf("iss" to "Corppass")),
            jwt.parseCorppass(result).filterKeys { key -> key != "exp" }
        )
    }

    @Test
    fun buildCorppassWithAdditionalInfoUrl() {
        val additionalInfoRequest = AdditionalInfoRequest()
        additionalInfoRequest.url = "http://localhost"
        additionalInfoRequest.httpMethod = "GET"

        every { mockCorppassProperties.additionalInfoRequest } returns additionalInfoRequest
        val result = jwt.buildCorppass(mockClaims)

        assertEquals(
            mockClaims.plus(mapOf("iss" to "Corppass", "additionalInfo" to mockAdditonalInfo)),
            jwt.parseCorppass(result).filterKeys { key -> key != "exp" }
        )
    }

    @Test
    fun buildCorppassWithAdditionalInfoStaticJson() {
        val additionalInfoRequest = AdditionalInfoRequest()
        additionalInfoRequest.url = null
        additionalInfoRequest.staticJson = "{\"static\":\"addition\"}"

        every { mockCorppassProperties.additionalInfoRequest } returns additionalInfoRequest
        val result = jwt.buildCorppass(mockClaims)

        assertEquals(
            mockClaims.plus(mapOf("iss" to "Corppass", "additionalInfo" to mapOf("static" to "addition"))),
            jwt.parseCorppass(result).filterKeys { key -> key != "exp" }
        )
    }

    @Test
    fun buildCorppassWithAdditionalInfoUrlError() {
        val additionalInfoRequest = AdditionalInfoRequest()
        additionalInfoRequest.url = "http://localhost/error"
        additionalInfoRequest.httpMethod = "GET"

        every { mockCorppassProperties.additionalInfoRequest } returns additionalInfoRequest
        try {
            jwt.buildCorppass(mockClaims)
            fail("Should be throwing AdditionalInfoRequestException when additionalInfoRequest has error")
        } catch (e: Exception) {
            assertTrue(e is AdditionalInfoRequestException)
        }
    }

    @Test
    fun buildOtherIssuerToken() {
        val additionalInfoRequest = AdditionalInfoRequest()
        additionalInfoRequest.url = "http://localhost"
        additionalInfoRequest.httpMethod = "GET"

        every { mockSingpassProperties.additionalInfoRequest } returns additionalInfoRequest
        every { mockCorppassProperties.additionalInfoRequest } returns additionalInfoRequest

        val result = jwt.build("Others", mockClaims)

        assertEquals(
            mockClaims.plus(mapOf("iss" to "Others")),
            jwt.parse("Others", result).filterKeys { key -> key != "exp" }
        )
    }
}
