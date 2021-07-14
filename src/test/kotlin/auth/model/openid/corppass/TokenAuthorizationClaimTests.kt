package auth.model.openid.corppass

import auth.model.corppass.AuthAccess
import auth.model.corppass.auth.Auth
import auth.model.corppass.auth.Parameter
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TokenAuthorizationClaimTests {
    val json = """
        {
            "iat": 1624086842,
            "exp": 1624087442,
            "aud": "vOIljWVrGyBMK6f31QYq",
            "iss": "https://stg-id.corppass.gov.sg"
        }"""

    @Test
    fun `openid authorization json with AuthInfo map to TokenAuthorizationClaim`() {
        val jsonWithAuthInfo = json.dropLast(1) + """
                ,
                "AuthInfo": {
                    "Result_Set": {
                        "ESrvc_Row_Count": 1,
                        "ESrvc_Result": [
                            {
                                "CPESrvcID": "JG016",
                                "Auth_Result_Set": {
                                    "Row_Count": 1,
                                    "Row": [
                                        {
                                            "CPEntID_SUB": "",
                                            "CPRole": "Role12345",
                                            "StartDate": "2019-04-15",
                                            "EndDate": "9999-12-31",
                                            "Parameter": [
                                                {
                                                    "name": "NONMANDATORYFT",
                                                    "value": ""
                                                }
                                            ]
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            }"""
        val mapper = ObjectMapper()
        val jsonMap = mapper.readValue(jsonWithAuthInfo, Map::class.java)
        val tokenAuthorizationClaim = mapper.convertValue(jsonMap, TokenAuthorizationClaim::class.java)

        val parameters = mutableListOf(
            Parameter(name = "NONMANDATORYFT", value = "")
        )
        val auths = mutableListOf<Auth>(
            Auth(role = "Role12345", startDate = "2019-04-15", endDate = "9999-12-31", parameters = parameters)
        )
        assertEquals(AuthAccess(auths).toMap(), tokenAuthorizationClaim.authAccess.toMap())
    }

    @Test
    fun `openid authorization json with TPAuthInfo map to TokenAuthorizationClaim`() {
        val jsonWithAuthInfo = json.dropLast(1) + """
                ,
                "TPAuthInfo": {
                    "Result_Set": {
                        "ESrvc_Row_Count": 1,
                        "ESrvc_Result": [
                            {
                                "CPESrvcID": "IRIN-ESRVC1",
                                "Auth_Set": {
                                    "ENT_ROW_COUNT": 1,
                                    "TP_Auth": [
                                        {
                                            "CP_Clnt_ID": "199206031W",
                                            "CP_ClntEnt_TYPE": "UEN",
                                            "Auth_Result_Set": {
                                                "Row_Count": 1,
                                                "Row": [
                                                    {
                                                        "CP_ClntEnt_SUB": "M12300678A",
                                                        "CPRole": "Preparer",
                                                        "StartDate": "2011-01-15",
                                                        "EndDate": "2011-01-15"
                                                    }
                                                ]
                                            }
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            }"""
        val mapper = ObjectMapper()
        val jsonMap = mapper.readValue(jsonWithAuthInfo, Map::class.java)
        val tokenAuthorizationClaim = mapper.convertValue(jsonMap, TokenAuthorizationClaim::class.java)

        assertEquals(1, tokenAuthorizationClaim.thirdPartyAuthAccess.clients.count())

        val firstClient = tokenAuthorizationClaim.thirdPartyAuthAccess.clients.get(0)
        assertEquals("199206031W", firstClient.entityId)
        assertEquals("UEN", firstClient.entityType)
        assertEquals(1, firstClient.auths.count())

        val firstClientAuth = firstClient.auths.get(0)
        assertEquals("M12300678A", firstClientAuth.subEntityId)
        assertEquals("Preparer", firstClientAuth.role)
        assertEquals("2011-01-15", firstClientAuth.startDate)
        assertEquals("2011-01-15", firstClientAuth.endDate)
    }
}
