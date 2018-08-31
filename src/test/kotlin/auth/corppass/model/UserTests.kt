package auth.corppass.model

import org.junit.Test
import org.junit.Assert.assertEquals
import java.util.Base64
import auth.helper.xml.DocumentBuilder
import auth.corppass.model.auth.Auth
import auth.corppass.model.auth.ThirdPartyClient
import auth.corppass.model.auth.Parameter

class UserTests {
    @Test
    fun castXMLToUser() {
        val xml = """
            <UserInfo>
                <CPAccType>User</CPAccType>
                <CPUID>F1234567P</CPUID>
                <CPUID_Country>SG</CPUID_Country>
                <CPUID_FullName>John Grisham</CPUID_FullName>
                <CPSystemUID>CP192</CPSystemUID>
                <ISSPHOLDER>YES</ISSPHOLDER>
                <CPEntID>R90SS0001A</CPEntID>
                <CPEnt_Status>Registered</CPEnt_Status>
                <CPEnt_TYPE>UEN</CPEnt_TYPE>
                <CPNonUEN_RegNo>NULL</CPNonUEN_RegNo>
                <CPNonUEN_Country>NULL</CPNonUEN_Country>
                <CPNonUEN_Name>NULL</CPNonUEN_Name>
            </UserInfo>
            <AuthAccess>
                <Result_Set>
                    <ESrvc_Row_Count>1</ESrvc_Row_Count>
                    <ESrvc_Result>
                        <CPESrvcID>BGESRV1</CPESrvcID>
                        <Auth_Result_Set>
                            <Row_Count>2</Row_Count>
                            <Row>
                                <CPEntID_SUB>NULL</CPEntID_SUB>
                                <CPRole>NULL</CPRole>
                                <StartDate>2016-01-15</StartDate>
                                <EndDate>2016-02-15</EndDate>
                            </Row>
                            <Row>
                                <CPEntID_SUB>NULL</CPEntID_SUB>
                                <CPRole>NULL</CPRole>
                                <StartDate>2016-03-15</StartDate>
                                <EndDate>2017-04-15</EndDate>
                                <Parameter name="Year of assessment">2014</Parameter>
                            </Row>
                        </Auth_Result_Set>
                    </ESrvc_Result>
                </Result_Set>
            </AuthAccess>
            <TPAuthAccess>
                <CP_TPEntID>R90SS0001A</CP_TPEntID>
                <CP_TPEnt_Status>Registered</CP_TPEnt_Status>
                <CP_TPEnt_TYPE>UEN</CP_TPEnt_TYPE>
                <Result_Set>
                    <ESrvc_Row_Count>1</ESrvc_Row_Count>
                    <ESrvc_Result>
                        <CPESrvcID>BGESRV1</CPESrvcID>
                        <Auth_Set>
                            <ENT_ROW_COUNT>1</ENT_ROW_COUNT>
                            <TP_Auth>
                                <CP_Clnt_ID>T15UF3564F</CP_Clnt_ID>
                                <CP_ClntEnt_TYPE>UEN</CP_ClntEnt_TYPE>
                                <Auth_Result_Set>
                                    <Row_Count>2</Row_Count>
                                    <Row>
                                        <CPEntID_SUB>NULL</CPEntID_SUB>
                                        <CPRole>NULL</CPRole>
                                        <StartDate>2016-01-15</StartDate>
                                        <EndDate>2016-02-15</EndDate>
                                    </Row>
                                    <Row>
                                        <CPEntID_SUB>NULL</CPEntID_SUB>
                                        <CPRole>NULL</CPRole>
                                        <StartDate>2016-03-15</StartDate>
                                        <EndDate>2017-04-15</EndDate>
                                        <Parameter name="Year of assessment">2014</Parameter>
                                    </Row>
                                </Auth_Result_Set>
                            </TP_Auth>
                        </Auth_Set>
                    </ESrvc_Result>
                </Result_Set>
            </TPAuthAccess>"""
        val base64Xml = Base64.getEncoder().encode(xml.toByteArray())
        val user = DocumentBuilder.parse<User>(String(base64Xml), "User")

        assertEquals("User", user.userInfo.accountType)
        assertEquals("F1234567P", user.userInfo.userId)
        assertEquals("SG", user.userInfo.userCountry)
        assertEquals("John Grisham", user.userInfo.userFullName)
        assertEquals("CP192", user.userInfo.userName)
        assertEquals(true, user.userInfo.singpassHolder)
        assertEquals("R90SS0001A", user.userInfo.entityId)
        assertEquals("Registered", user.userInfo.entityStatus)
        assertEquals("UEN", user.userInfo.entityType)
        assertEquals(null, user.userInfo.entityRegNo)
        assertEquals(null, user.userInfo.entityCountry)
        assertEquals(null, user.userInfo.entityName)

        val parameters = mutableListOf(
            Parameter(name = "Year of assessment", value = "2014")
        )
        val auths = mutableListOf<Auth>(
            Auth(startDate = "2016-01-15", endDate = "2016-02-15"),
            Auth(startDate = "2016-03-15", endDate = "2017-04-15", parameters = parameters)
        )
        val authAccess = AuthAccess(auths)
        assertEquals(authAccess.toMap(), user.authAccess.toMap())

        val clients = mutableListOf(
            ThirdPartyClient("T15UF3564F", "UEN", auths)
        )
        val thirdPartyAuthAccess = ThirdPartyAuthAccess("R90SS0001A", "Registered", "UEN", clients)
        assertEquals(thirdPartyAuthAccess.toMap(), user.thirdPartyAuthAccess.toMap())

        assertEquals(mapOf(
            "userInfo" to mapOf(
                "accountType" to "User",
                "userId" to "F1234567P",
                "userCountry" to "SG",
                "userFullName" to "John Grisham",
                "userName" to "CP192",
                "singpassHolder" to true,
                "entityId" to "R90SS0001A",
                "entityStatus" to "Registered",
                "entityType" to "UEN",
                "entityRegNo" to null,
                "entityCountry" to null,
                "entityName" to null
            ),
            "authAccess" to authAccess.toMap(),
            "thirdPartyAuthAccess" to thirdPartyAuthAccess.toMap()
        ),
        user.toMap())
    }
}
