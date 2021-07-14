package auth.service.singpass

import auth.model.singpass.User
import auth.util.helper.AdditionalInfoRequest
import auth.util.helper.Properties
import auth.util.helper.Provider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class TokenServiceTests {
    val mockAdditonalInfo = mapOf("addition" to "info")
    val mockUser = User()

    @MockK
    lateinit var mockProperties: Properties
    @MockK
    lateinit var mockSingpass: Provider
    @MockK
    lateinit var mockAdditionalInfoRequest: AdditionalInfoRequest

    @SpyK
    var tokenService = TokenService()

    @BeforeAll
    fun setupMock() {
        MockKAnnotations.init(this)
        tokenService.properties = mockProperties
        every { mockProperties.singpass } returns mockSingpass
        every { mockSingpass.additionalInfoRequest } returns mockAdditionalInfoRequest

        every {
            tokenService.build(ISSUER, mockUser.toMap())
        } returns "token.without.additionalInfo"
        every {
            tokenService.build(ISSUER, mockUser.toMap().plus(mapOf("additionalInfo" to mockAdditonalInfo)))
        } returns "token.with.additionalInfo"
    }

    @Test
    fun `return token with additionalInfo payload when there is additionalInfo`() {
        every {
            mockAdditionalInfoRequest.resolveAdditionalInfo(
                mapOf("Authorization" to "Bearer token.without.additionalInfo")
            )
        } returns mockAdditonalInfo

        val token = tokenService.build(mockUser)

        assertEquals("token.with.additionalInfo", token)
    }

    @Test
    fun `return token without additionalInfo payload when there is no additionalInfo`() {
        every {
            mockAdditionalInfoRequest.resolveAdditionalInfo(
                mapOf("Authorization" to "Bearer token.without.additionalInfo")
            )
        } returns null

        val token = tokenService.build(mockUser)

        assertEquals("token.without.additionalInfo", token)
    }
}
