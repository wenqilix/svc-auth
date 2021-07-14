package auth.model.corppass

import auth.model.corppass.auth.Parameter
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ThirdPartyAuthAccessTests {
    @Test
    fun `Cast json to ThirdPartyAuthAccess`() {
        val json = """
            {
                "Result_Set": {
                    "ESrvc_Row_Count": 1,
                    "ESrvc_Result": [
                        {
                            "CPESrvcID": "IRIN-ESRVC1",
                            "Auth_Set": {
                                "ENT_ROW_COUNT": 2,
                                "TP_Auth": [
                                    {
                                        "CP_Clnt_ID": "T15UF3564F",
                                        "CP_ClntEnt_TYPE": "UEN",
                                        "Auth_Result_Set": {
                                            "Row_Count": 2,
                                            "Row": [
                                                {
                                                    "CP_ClntEnt_SUB": "M12345678X",
                                                    "CPRole": "",
                                                    "StartDate": "2011-01-15",
                                                    "EndDate": "2011-01-15",
                                                    "Parameter": [
                                                        {
                                                            "name": "Year of assessment",
                                                            "value": "2015"
                                                        }
                                                    ]
                                                },
                                                {
                                                    "CP_ClntEnt_SUB": "M19945678X",
                                                    "CPRole": "Approver",
                                                    "StartDate": "2011-01-15",
                                                    "EndDate": "2011-01-15",
                                                    "Parameter": [
                                                        {
                                                            "name": "Year of assessment",
                                                            "value": "2014"
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    },
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
                                                    "EndDate": "2011-01-15",
                                                    "Parameter": [
                                                        {
                                                            "name": "Year of assessment",
                                                            "value": "2014"
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                }
            }"""

        val mapper = ObjectMapper()
        val thirdPartyAuthAccess = mapper.readValue(json, ThirdPartyAuthAccess::class.java)

        assertEquals(2, thirdPartyAuthAccess.clients.count())

        val firstClient = thirdPartyAuthAccess.clients.get(0)
        assertEquals("T15UF3564F", firstClient.entityId)
        assertEquals("UEN", firstClient.entityType)
        assertEquals(2, firstClient.auths.count())

        val firstClientFirstAuth = firstClient.auths.get(0)
        assertEquals("M12345678X", firstClientFirstAuth.subEntityId)
        assertEquals(null, firstClientFirstAuth.role)
        assertEquals("2011-01-15", firstClientFirstAuth.startDate)
        assertEquals("2011-01-15", firstClientFirstAuth.endDate)
        assertEquals(
            mutableListOf(
                Parameter(name = "Year of assessment", value = "2015")
            ),
            firstClientFirstAuth.parameters
        )

        val firstClientSecondAuth = firstClient.auths.get(1)
        assertEquals("M19945678X", firstClientSecondAuth.subEntityId)
        assertEquals("Approver", firstClientSecondAuth.role)
        assertEquals("2011-01-15", firstClientSecondAuth.startDate)
        assertEquals("2011-01-15", firstClientSecondAuth.endDate)
        assertEquals(
            mutableListOf(
                Parameter(name = "Year of assessment", value = "2014")
            ),
            firstClientSecondAuth.parameters
        )

        val secondClient = thirdPartyAuthAccess.clients.get(1)
        assertEquals("199206031W", secondClient.entityId)
        assertEquals("UEN", secondClient.entityType)
        assertEquals(1, secondClient.auths.count())

        val secondClientSecondAuth = secondClient.auths.get(0)
        assertEquals("M12300678A", secondClientSecondAuth.subEntityId)
        assertEquals("Preparer", secondClientSecondAuth.role)
        assertEquals("2011-01-15", secondClientSecondAuth.startDate)
        assertEquals("2011-01-15", secondClientSecondAuth.endDate)
        assertEquals(
            mutableListOf(
                Parameter(name = "Year of assessment", value = "2014")
            ),
            secondClientSecondAuth.parameters
        )
    }
}
