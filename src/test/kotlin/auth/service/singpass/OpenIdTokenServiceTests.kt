package auth.service.singpass

import auth.model.openid.TokenResponse
import auth.util.helper.Properties
import auth.util.jwt.TokenParser
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
class OpenIdTokenServiceTests {
    val tokenResponse = TokenResponse(
        accessToken = "sQRKiEhTdc8hch5cGHbgTb8DGx9G8D",
        refreshToken = "UATDhPb4QGpyjyO3qieMrHEiiHTCIiOm0jAudIQx",
        scope = "openid",
        idToken = """
            eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwia2l
            kIjoidGQzRVRGZXZuWUxGVENDeks0cklIbmVqR3RWZldyZ3U3UUpLU2ZyYWdmNCIsImN0eSI6I
            kpXVCJ9.eiElCMM1XIge9bopN_-
            M6ZykUPIhhEuFmkp__ViJ680cNz2hLzoqFU0z88gY6NzyBDhzP8XCiqylOCI45roXPOJgpx244aOF
            -sn0juQb3nGaAL6wL2eWTYEDrWDDiMcxdsSCT--
            PwzvDf6N6F3CJNgKhgPDv0WEzHiz0FWrI7AiQM5-
            3DMsaFx8H3zbUf9QTzjAL_1oApdCuNDksDZEzIpXQZhdIgoL1dAwkyuPMRhKU_sO8a4f5wLh3fp
            MmHPm5s7WtBbkfggBze6ZXeDi5_zyDvjRZhCMza_QfHrzneQpNgySs6v1ZPyHt0ONi8GAh0l3hzg
            YxKxjvxyaylW5JtQ.8eY1wqvYBXIJYxrLrpxnZw.oBi27sBL_R-
            otVr46g29uuWUBCDd9sVa0vfjKjVh9uHyMGNN0vOku1yV4BH7tRm-ClGFFrl_g-
            H3S0R14gQgXcv6gLubceupBmE6mPWaQwyAlW3yFzvJ_FQRx9y26brrkZIx8ehMx-6UwIeF-
            bIznLkCqBEyp5eE47VmN817PkvCoN4NckUmlSljTSejKNB8I_Y4l6FiEkb02XBHxbhdFRnMwRUN
            W8Uiadny8qXT4kRHTkCqwxpCm0bGwOIXQr-
            0x203Rp4Dzu1Aw9VSdmuyAbwLvp503GwdBo1Pe0ABi2RA8clPxroKYxfoz0VYRgnlJRvn-
            z62uEXsEfBaZ7AdOGC9mravC7EogDqobEjvSbT8TFDPkYB4EmV061ZfUe8pnr13ID0t2NuELETA
            ejXfCMiYTg0hB9HB7dlk-zh5d19Dqcxo4if_MaYKrtLeVh-CJCgU24VBZGpdQNuG0ZodmrFNJsIoi-
            f7ragobCxv2S4Y-fbCCSuHQWbwpgQcBXSAFwhwPDoX0u-1Fh8nJlr2-
            uo3EoLae36Zmyl7F8ndLIzx1iHg3J2tfOYFExeByquNRGfqto6L6kRzzjnBl-
            NOzwCx51dD6IIKZmrHbZNbWgdPcuqeoc4gL1ciCjwm4gQk-bdwg0JkInKOULO6gf47gPjkz6owlBHoH9P_Qp4p6Frh5zW87hpzPC7RMUAvwFrWn74VYqQK1Gh
            UiktlYG8ys-
            ek5GNsbxD7n2urmN6NfQOJ8SZ83ewoTHcRC1gN9F8w9QeOoqsCUt76Aupze5c614tudIAHUJL1i
            00oMevNWWm9pb3OlVGENtUrMf5HYPHuIn6C57BuXVtEkiWZA6SQ-
            KgJ29NKhUY3nItpvzKrhWpn1tysFtPP_oNIKIcgBqXltDSjLzJ8BNfhZdMrRJwosZm4t4I9v98Yvj35n
            WFA-gxGl8uqt-MZ4D3-on4P0lSRjknqeNClx4qb6ilEniR3i8BW7ktvBbpN8ueicS47_H0.-E6iFnM-
            mX7W2VeDmO1WDXSM2Wvi_PqwRmU50ATgAKk
        """,
        tokenType = "bearer",
        expiresIn = 600
    )

    @MockK
    lateinit var mockProperties: Properties
    @MockK
    lateinit var mockTokenParser: TokenParser

    @SpyK
    var openIdTokenService = OpenIdTokenService()

    @BeforeAll
    fun setupMock() {
        MockKAnnotations.init(this)
        openIdTokenService.properties = mockProperties

        every {
            openIdTokenService.tokenParser
        } returns mockTokenParser
    }

    @Test
    fun `return user from openId token response`() {
        every {
            mockTokenParser.parse(tokenResponse.idToken)
        } returns mapOf(
            "rt_hash" to "YJkblivCTzQaht9xDMCORg",
            "nonce" to "a2ghskf1234las",
            "amr" to listOf("[\"pwd\"]"),
            "iat" to 1545822418,
            "iss" to "https://stg-saml.singpass.gov.sg",
            "at_hash" to "ou-aCvmEC4HFzN_LEItY6w",
            "sub" to "s=S9000000E,u=123e4567-e89b-12d6-a456-426655440000",
            "exp" to 1545826018,
            "aud" to "TESTRP-01"
        )

        val user = openIdTokenService.parse(tokenResponse)

        assertEquals("S9000000E", user.userName)
        assertEquals(tokenResponse.refreshToken, user.refreshToken)
    }
}
