package auth.model.corppass

import auth.model.corppass.auth.Auth
import auth.model.corppass.auth.Parameter
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuthAccessTests {
    @Test
    fun `cast json to AuthAccess`() {
        val json = """
            {"Result_Set": {
                "ESrvc_Row_Count": 1,
                "ESrvc_Result": [
                    {
                        "CPESrvcID": "BGESRV1",
                        "Auth_Result_Set": {
                            "Row_Count": 2,
                            "Row": [
                                {
                                    "CPEntID_SUB": "",
                                    "CPRole": "",
                                    "StartDate": "2016-01-15",
                                    "EndDate": "2016-02-15"
                                },
                                {
                                    "CPEntID_SUB": "",
                                    "CPRole": "",
                                    "StartDate": "2016-03-15",
                                    "EndDate": "2017-04-15",
                                    "Parameter": [
                                        {
                                            "name": "Year of assessment",
                                            "value": "2014"
                                        },
                                        {
                                            "name": "other02",
                                            "value": "value 02"
                                        },
                                        {
                                            "name": "other03",
                                            "value": "value 03"},
                                        {
                                            "name": "other04",
                                            "value": "value 04"
                                        },
                                        {
                                            "name": "other05",
                                            "value": "value 05"
                                        },
                                        {
                                            "name": "other06",
                                            "value": "value 06"
                                        },
                                        {
                                            "name": "other07",
                                            "value": "value 07"
                                        },
                                        {
                                            "name": "other08",
                                            "value": "value 08"
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                ]
            }}"""

        val mapper = ObjectMapper()
        val authAccess = mapper.readValue(json, AuthAccess::class.java)

        val parameters = mutableListOf(
            Parameter(name = "Year of assessment", value = "2014"),
            Parameter(name = "other02", value = "value 02"),
            Parameter(name = "other03", value = "value 03"),
            Parameter(name = "other04", value = "value 04"),
            Parameter(name = "other05", value = "value 05"),
            Parameter(name = "other06", value = "value 06"),
            Parameter(name = "other07", value = "value 07"),
            Parameter(name = "other08", value = "value 08")
        )
        val auths = mutableListOf<Auth>(
            Auth(startDate = "2016-01-15", endDate = "2016-02-15"),
            Auth(startDate = "2016-03-15", endDate = "2017-04-15", parameters = parameters)
        )

        assertEquals(AuthAccess(auths).toMap(), authAccess.toMap())
    }
}
