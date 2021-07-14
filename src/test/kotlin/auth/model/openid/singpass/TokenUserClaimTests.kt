package auth.model.openid.singpass

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TokenUserClaimTests {
    @Test
    fun `openid singpass id token json map to TokenUserClaim`() {
        val json = """
            {
                "rt_hash": "YJkblivCTzQaht9xDMCORg",
                "nonce": "a2ghskf1234las",
                "amr": [ "[\"pwd\"]" ],
                "iat": 1545822418,
                "iss": "https://stg-saml.singpass.gov.sg",
                "at_hash": "ou-aCvmEC4HFzN_LEItY6w",
                "sub":"s=S9000000E,u=123e4567-e89b-12d6-a456-426655440000",
                "exp": 1545826018,
                "aud": "TESTRP-01"
            }"""
        val mapper = ObjectMapper()
        val jsonMap = mapper.readValue(json, Map::class.java)
        val tokenUserClaim = mapper.convertValue(jsonMap, TokenUserClaim::class.java)

        assertEquals("S9000000E", tokenUserClaim.subjectInfo.userName)
        assertEquals("123e4567-e89b-12d6-a456-426655440000", tokenUserClaim.subjectInfo.uuid)
    }
}
