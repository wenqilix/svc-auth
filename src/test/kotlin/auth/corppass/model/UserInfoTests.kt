package auth.corppass.model

import org.junit.Test
import org.junit.Assert.assertEquals
import auth.helper.xml.DocumentBuilder
import java.io.ByteArrayInputStream

class UserInfoTests {
    @Test
    fun castUenXmlToUserInfo() {
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
            </UserInfo>"""

        val userInfo = DocumentBuilder.parse<UserInfo>(ByteArrayInputStream(xml.toByteArray()))

        assertEquals("User", userInfo.accountType)
        assertEquals("F1234567P", userInfo.userId)
        assertEquals("SG", userInfo.userCountry)
        assertEquals("John Grisham", userInfo.userFullName)
        assertEquals("CP192", userInfo.userName)
        assertEquals(true, userInfo.singpassHolder)
        assertEquals("R90SS0001A", userInfo.entityId)
        assertEquals("Registered", userInfo.entityStatus)
        assertEquals("UEN", userInfo.entityType)
        assertEquals(null, userInfo.entityRegNo)
        assertEquals(null, userInfo.entityCountry)
        assertEquals(null, userInfo.entityName)

        assertEquals(mapOf(
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
        userInfo.toMap())
    }

    @Test
    fun castNonUenXmlToUserInfo() {
        val xml = """
            <UserInfo>
                <CPAccType>Admin</CPAccType>
                <CPUID>Z9239556A</CPUID>
                <CPUID_Country>CO</CPUID_Country>
                <CPUID_FullName>JUAN VALDEZ</CPUID_FullName>
                <CPSystemUID>CP25880</CPSystemUID>
                <ISSPHOLDER>NO</ISSPHOLDER>
                <CPEntID>C18000545L</CPEntID>
                <CPEnt_Status>NULL</CPEnt_Status>
                <CPEnt_TYPE>NON-UEN</CPEnt_TYPE>
                <CPNonUEN_RegNo>999999999</CPNonUEN_RegNo>
                <CPNonUEN_Country>CO</CPNonUEN_Country>
                <CPNonUEN_Name>JUAN VALDEZ</CPNonUEN_Name>
            </UserInfo>"""

        val userInfo = DocumentBuilder.parse<UserInfo>(ByteArrayInputStream(xml.toByteArray()))

        assertEquals("Admin", userInfo.accountType)
        assertEquals("Z9239556A", userInfo.userId)
        assertEquals("CO", userInfo.userCountry)
        assertEquals("JUAN VALDEZ", userInfo.userFullName)
        assertEquals("CP25880", userInfo.userName)
        assertEquals(false, userInfo.singpassHolder)
        assertEquals("C18000545L", userInfo.entityId)
        assertEquals(null, userInfo.entityStatus)
        assertEquals("NON-UEN", userInfo.entityType)
        assertEquals("999999999", userInfo.entityRegNo)
        assertEquals("CO", userInfo.entityCountry)
        assertEquals("JUAN VALDEZ", userInfo.entityName)

        assertEquals(mapOf(
            "accountType" to "Admin",
            "userId" to "Z9239556A",
            "userCountry" to "CO",
            "userFullName" to "JUAN VALDEZ",
            "userName" to "CP25880",
            "singpassHolder" to false,
            "entityId" to "C18000545L",
            "entityStatus" to null,
            "entityType" to "NON-UEN",
            "entityRegNo" to "999999999",
            "entityCountry" to "CO",
            "entityName" to "JUAN VALDEZ"
        ),
        userInfo.toMap())
    }
}
