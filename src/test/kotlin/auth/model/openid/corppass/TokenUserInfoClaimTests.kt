package auth.model.openid.corppass

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TokenUserInfoClaimTests {
    @Test
    fun `openid userinfo json map to TokenUserInfoClaim`() {
        val json = """
            {
                "iat": 1623162109,
                "iss": "https://stg-id.corppass.gov.sg",
                "at_hash": "6J4VlBBQpbAyy1NL4NBW-Q",
                "sub": "s=S1234567P, u=CP192, c=SG",
                "exp": 1623165709,
                "aud": "vOIljWVrGyBMK6f31QYq",
                "amr": ["pwd", "sms"],
                "nonce": "ZEF+97zc3YZP7huv6nzKspfabDv0wRtce/aVNud23vU=",
                "userInfo":{
                    "CPAccType": "User",
                    "CPUID_FullName": "John Grisham",
                    "ISSPHOLDER": "YES"
                },
                "entityInfo": {
                    "CPEntID": "82532759L",
                    "CPEnt_TYPE": "UEN",
                    "CPEnt_Status": "Registered",
                    "CPNonUEN_Country": "",
                    "CPNonUEN_RegNo": "",
                    "CPNonUEN_Name": ""
                }
            }"""
        val mapper = ObjectMapper()
        val jsonMap = mapper.readValue(json, Map::class.java)
        val tokenUserInfoClaim = mapper.convertValue(jsonMap, TokenUserInfoClaim::class.java)

        assertEquals("User", tokenUserInfoClaim.userInfo.accountType)
        assertEquals("John Grisham", tokenUserInfoClaim.userInfo.userFullName)
        assertEquals(true, tokenUserInfoClaim.userInfo.singpassHolder)
        assertEquals("S1234567P", tokenUserInfoClaim.subjectInfo.userId)
        assertEquals("SG", tokenUserInfoClaim.subjectInfo.userCountry)
        assertEquals("CP192", tokenUserInfoClaim.subjectInfo.userName)

        assertEquals("82532759L", tokenUserInfoClaim.entityInfo.entityId)
        assertEquals("Registered", tokenUserInfoClaim.entityInfo.entityStatus)
        assertEquals("UEN", tokenUserInfoClaim.entityInfo.entityType)
        assertEquals(null, tokenUserInfoClaim.entityInfo.entityRegNo)
        assertEquals(null, tokenUserInfoClaim.entityInfo.entityCountry)
        assertEquals(null, tokenUserInfoClaim.entityInfo.entityName)
    }
}
