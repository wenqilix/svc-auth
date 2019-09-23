package auth.singpass

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import auth.helper.Jwt
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.MockKAnnotations
import io.mockk.every

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiControllerIntegrationTests {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun refresh() {
        val result = testRestTemplate.getForEntity("/sp/refresh", String::class.java)
        assertNotNull(result)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }
}

class ApiControllerUnitTests {

    @MockK
    lateinit var mockJwt: Jwt

    @InjectMockKs
    lateinit var apiController: ApiController

    @Before
    fun setupMock() {
        MockKAnnotations.init(this)
    }

    @Test
    fun refresh() {
        val validToken = "valid.token.value"
        every { mockJwt.parseSingpass(validToken) } returns mapOf("userName" to "foo", "mobile" to "bar")
        every { mockJwt.buildSingpass(mapOf("userName" to "foo", "mobile" to "bar")) } returns "refreshed.token.value"

        val result = apiController.refresh("Bearer $validToken")
        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(listOf("refreshed.token.value"), result.getHeaders().get(ApiController.HEADER_STRING))
    }

    @Test
    fun refreshWithInvalidToken() {
        val invalidToken = "general.invalid.token"
        every { mockJwt.parseSingpass(invalidToken) } throws RuntimeException()

        var result = apiController.refresh(invalidToken)
        assertNotNull(result)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }
}
