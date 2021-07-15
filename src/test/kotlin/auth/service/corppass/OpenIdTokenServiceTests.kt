package auth.service.corppass

import auth.model.corppass.AuthAccess
import auth.model.corppass.ThirdPartyAuthAccess
import auth.model.corppass.UserInfo
import auth.model.corppass.auth.Auth
import auth.model.corppass.auth.Parameter
import auth.model.openid.TokenResponse
import auth.util.helper.Properties
import auth.util.jwt.TokenParser
import com.fasterxml.jackson.databind.ObjectMapper
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
    val tokenResponsePayload = """
        {
            "access_token": "eyJraWQiOiJueGlKSk5OVnh4blRrVTJ3TDY1VEkyUGtJTElKT1VSU1RRU0FCTHVIMmtFIiwiYWxnIjoiRVMyNTYifQ.eyJleHAiOjE2MjQwODQ4MjMsImlhdCI6MTYyNDA4NDIyMywiaXNzIjoiaHR0cHM6Ly9jb3JwcGFzc3JwMDMiLCJhdWQiOiJ2T0lsaldWckd5Qk1LNmYzMVFZcSIsInNjb3BlIjpbImF1dGhpbmZvIiwidHBhdXRoaW5mbyJdfQ.jzzTSQw5w3B_88KgqzThrP237wYWkuuzBdRpHnT4iNoqWAjV8KsXDqwBgOnECsFBeshJXahMDswyqw6xlgTO9g",
            "scope": "openid",
            "id_token": "eyJraWQiOiJueGlKSk5OVnh4blRrVTJ3TDY1VEkyUGtJTElKT1VSU1RRU0FCTHVIMmtFIiwiYWxnIjoiRVMyNTYifQ.eyJlbnRpdHlJbmZvIjp7IkNQRW50SUQiOiJWQlIwMDAwMDQiLCJDUEVudF9UWVBFIjoiVUVOIiwiQ1BFbnRfU3RhdHVzIjoiUmVnaXN0ZXJlZCIsIkNQTm9uVUVOX0NvdW50cnkiOiIiLCJDUE5vblVFTl9SZWdObyI6IiIsIkNQTm9uVUVOX05hbWUiOiIifSwiYW1yIjpbInB3ZCJdLCJpYXQiOjE2MjQwODQyMjIsImlzcyI6Imh0dHBzOi8vY29ycHBhc3NycDAzIiwic3ViIjoicz1udWxsLHU9YW1pdGVzaCxjPW51bGwiLCJhdF9oYXNoIjoiMncxWjlBNW9qZExic2hSLUIwbFV4QSIsImV4cCI6MTYyNDA4NzgyMiwiYXVkIjoidk9JbGpXVnJHeUJNSzZmMzFRWXEifQ.lwTieBCXxOHMtKFkpLZDarzGe5QsZiFnZoWxVoSPLEzPhTABMgStGknzlf9m1hZiw6rCP_4InngFNLeh8DeztA",
            "token_type": "bearer",
            "expires_in": 599
        }"""
    val mapper = ObjectMapper()
    val tokenResponse = mapper.convertValue(
        mapper.readValue(tokenResponsePayload, Map::class.java),
        TokenResponse::class.java
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

        every {
            mockTokenParser.parse(tokenResponse.idToken)
        } returns mapOf(
            "userInfo" to mapOf(
                "CPAccType" to "Admin",
                "CPUID_FullName" to "ANG KEVIN",
                "ISSPHOLDER" to "YES"
            ),
            "entityInfo" to mapOf(
                "CPEntID" to "82532759L",
                "CPEnt_TYPE" to "UEN",
                "CPEnt_Status" to "Registered",
                "CPNonUEN_Country" to "",
                "CPNonUEN_RegNo" to "",
                "CPNonUEN_Name" to ""
            ),
            "rt_hash" to "0P61rRVWtI0OVbrGNo5PXg",
            "nonce" to "zcn0E3HJDe",
            "amr" to listOf("pwd"),
            "iat" to 1558429072,
            "iss" to "https://qa.corppass.gov.sg",
            "sub" to "s=S3892835Z,u=CP8202,c=SG",
            "at_hash" to "2KvUAjFGby4JR_Q8Y9y_Gg",
            "exp" to 1558432672,
            "aud" to "JG016"
        )
    }

    @Test
    fun `return user from openId token response`() {
        tokenResponse.authorizationInfoToken = "some.auth.token"
        every {
            mockTokenParser.parse(tokenResponse.authorizationInfoToken!!)
        } returns mapOf(
            "exp" to 1558429672,
            "iat" to 1558429072,
            "iss" to "https://qa.corppass.gov.sg",
            "aud" to "JG016",
            "AuthInfo" to mapOf(
                "Result_Set" to mapOf(
                    "ESrvc_Row_Count" to 1,
                    "ESrvc_Result" to listOf(
                        mapOf(
                            "CPESrvcID" to "JG016",
                            "Auth_Result_Set" to mapOf(
                                "Row_Count" to 1,
                                "Row" to listOf(
                                    mapOf(
                                        "CPEntID_SUB" to "",
                                        "CPRole" to "Role12345",
                                        "StartDate" to "2019-04-15",
                                        "EndDate" to "9999-12-31",
                                        "Parameter" to listOf(
                                            mapOf(
                                                "name" to "NONMANDATORYFT",
                                                "value" to ""
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        val user = openIdTokenService.parse(tokenResponse)

        val expectedUserInfo = UserInfo(
            accountType = "Admin",
            userId = "S3892835Z",
            userCountry = "SG",
            userFullName = "ANG KEVIN",
            userName = "CP8202",
            singpassHolder = true,
            entityId = "82532759L",
            entityStatus = "Registered",
            entityType = "UEN"
        )
        val expectedAuthAccess = AuthAccess(
            mutableListOf(
                Auth(
                    role = "Role12345",
                    startDate = "2019-04-15",
                    endDate = "9999-12-31",
                    parameters = mutableListOf(
                        Parameter(name = "NONMANDATORYFT", value = "")
                    )
                )
            )
        )
        assertEquals(expectedUserInfo, user.userInfo)
        assertEquals(expectedAuthAccess, user.authAccess)
        assertEquals(ThirdPartyAuthAccess(), user.thirdPartyAuthAccess)
        assertEquals(tokenResponse.refreshToken, user.refreshToken)
    }

    @Test
    fun `able to resolve user when authorizationInfoToken is null`() {
        val tokenResponseWithoutAuthorizationInfo = TokenResponse(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            idToken = tokenResponse.idToken,
            tokenType = tokenResponse.tokenType,
            expiresIn = tokenResponse.expiresIn,
            authorizationInfoToken = null
        )
        val user = openIdTokenService.parse(tokenResponseWithoutAuthorizationInfo)

        val expectedUserInfo = UserInfo(
            accountType = "Admin",
            userId = "S3892835Z",
            userCountry = "SG",
            userFullName = "ANG KEVIN",
            userName = "CP8202",
            singpassHolder = true,
            entityId = "82532759L",
            entityStatus = "Registered",
            entityType = "UEN"
        )
        assertEquals(expectedUserInfo, user.userInfo)
        assertEquals(AuthAccess(), user.authAccess)
        assertEquals(ThirdPartyAuthAccess(), user.thirdPartyAuthAccess)
        assertEquals(tokenResponse.refreshToken, user.refreshToken)
    }
}
