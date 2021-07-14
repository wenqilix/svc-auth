package auth.controller.corppass

import auth.service.corppass.LoginService
import auth.service.corppass.OpenIdTokenService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ApiController::class)
internal class ApiControllerTests {
    @MockkBean
    private lateinit var mockLoginService: LoginService
    @MockkBean
    private lateinit var expected: OpenIdTokenService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `refresh - should return new token when valid token is provided`() {
        val validToken = "valid.token.value"
        every { mockLoginService.refreshToken(validToken) } returns "refreshed.token.value"

        mockMvc.perform(
            get("/cp/refresh").header(ApiController.HEADER_STRING, "Bearer $validToken")
        ).andExpect(status().isOk).andExpect(header().string(ApiController.HEADER_STRING, "refreshed.token.value"))
    }

    @Test
    fun `refresh - should return 401 unauthorized when invalid token provided`() {
        val invalidToken = "general.invalid.token"
        every { mockLoginService.refreshToken(invalidToken) } throws InvalidJwtException("", listOf(), JwtContext(JwtClaims(), listOf()))

        mockMvc.perform(
            get("/cp/refresh").header(ApiController.HEADER_STRING, "Bearer $invalidToken")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `refresh - should return 400 bad request when error occured`() {
        val errorToken = "general.error.token"
        every { mockLoginService.refreshToken(errorToken) } throws RuntimeException()

        mockMvc.perform(
            get("/cp/refresh").header(ApiController.HEADER_STRING, "Bearer $errorToken")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `token - should return token when valid authCode provided`() {
        val validAuthCode = "someValidCode"
        every { mockLoginService.getToken(validAuthCode) } returns "valid.token.value"

        mockMvc.perform(
            get("/cp/token").param("authCode", validAuthCode)
        ).andExpect(status().isOk).andExpect(header().string(ApiController.HEADER_STRING, "valid.token.value"))
    }

    @Test
    fun `token - should return 400 bad request when invalid authCode provided`() {
        val invalidAuthCode = "invalidCode"
        every { mockLoginService.getToken(invalidAuthCode) } throws RuntimeException()

        mockMvc.perform(
            get("/cp/token").param("authCode", invalidAuthCode)
        ).andExpect(status().isBadRequest)
    }
}
