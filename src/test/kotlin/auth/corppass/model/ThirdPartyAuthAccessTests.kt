package auth.corppass.model

import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.util.Arrays
import auth.helper.xml.DocumentBuilder
import auth.corppass.model.auth.*

class ThirdPartyAuthAccessTests {
    @Test
    fun castXMLToThirdPartyAuthAccess() {
        val xml = """
            <TPAuthAccess>
                <CP_TPEntID>78129384P</CP_TPEntID>
                <CP_TPEnt_Status>Registered</CP_TPEnt_Status>
                <CP_TPEnt_TYPE>UEN</CP_TPEnt_TYPE>
                <Result_Set>
                    <ESrvc_Row_Count>1</ESrvc_Row_Count>
                    <ESrvc_Result>
                        <CPESrvcID>IRIN-ESRVC1</CPESrvcID>
                        <Auth_Set>
                            <ENT_ROW_COUNT>2</ENT_ROW_COUNT>
                            <TP_Auth>
                                <CP_Clnt_ID>T15UF3564F</CP_Clnt_ID>
                                <CP_ClntEnt_TYPE>UEN</CP_ClntEnt_TYPE>
                                <Auth_Result_Set>
                                    <Row_Count>2</Row_Count>
                                    <Row>
                                        <CP_ClntEnt_SUB>M12345678X</CP_ClntEnt_SUB>
                                        <CPRole>NULL</CPRole>
                                        <StartDate>2011-01-15</StartDate>
                                        <EndDate>2011-01-15</EndDate>
                                        <Parameter name="Year of assessment">2015</Parameter>
                                    </Row>
                                    <Row>
                                        <CP_ClntEnt_SUB>M19945678X</CP_ClntEnt_SUB>
                                        <CPRole>Approver</CPRole>
                                        <StartDate>2011-01-15</StartDate>
                                        <EndDate>2011-01-15</EndDate>
                                        <Parameter name="Year of assessment">2014</Parameter>
                                    </Row>
                                </Auth_Result_Set>
                            </TP_Auth>
                            <TP_Auth>
                                <CP_Clnt_ID>199206031W</CP_Clnt_ID>
                                <CP_ClntEnt_TYPE>UEN</CP_ClntEnt_TYPE>
                                <Auth_Result_Set>
                                    <Row_Count>1</Row_Count>
                                    <Row>
                                        <CP_ClntEnt_SUB>M12300678A</CP_ClntEnt_SUB>
                                        <CPRole>Preparer</CPRole>
                                        <StartDate>2011-01-15</StartDate>
                                        <EndDate>2011-01-15</EndDate>
                                        <Parameter name="Year of assessment">2014</Parameter>
                                    </Row>
                                </Auth_Result_Set>
                            </TP_Auth>
                        </Auth_Set>
                    </ESrvc_Result>
                </Result_Set>
            </TPAuthAccess>"""

        val thirdPartyAuthAccess = DocumentBuilder.parse<ThirdPartyAuthAccess>(ByteArrayInputStream(xml.toByteArray()))

        assertEquals("78129384P", thirdPartyAuthAccess.entityId)
        assertEquals("Registered", thirdPartyAuthAccess.entityStatus)
        assertEquals("UEN", thirdPartyAuthAccess.entityType)
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
        assertEquals(mutableListOf(
            Parameter(name="Year of assessment", value="2015")
        ), firstClientFirstAuth.parameters)

        val firstClientSecondAuth = firstClient.auths.get(1)
        assertEquals("M19945678X", firstClientSecondAuth.subEntityId)
        assertEquals("Approver", firstClientSecondAuth.role)
        assertEquals("2011-01-15", firstClientSecondAuth.startDate)
        assertEquals("2011-01-15", firstClientSecondAuth.endDate)
        assertEquals(mutableListOf(
            Parameter(name="Year of assessment", value="2014")
        ), firstClientSecondAuth.parameters)

        val secondClient = thirdPartyAuthAccess.clients.get(1)
        assertEquals("199206031W", secondClient.entityId)
        assertEquals("UEN", secondClient.entityType)
        assertEquals(1, secondClient.auths.count())

        val secondClientSecondAuth = secondClient.auths.get(0)
        assertEquals("M12300678A", secondClientSecondAuth.subEntityId)
        assertEquals("Preparer", secondClientSecondAuth.role)
        assertEquals("2011-01-15", secondClientSecondAuth.startDate)
        assertEquals("2011-01-15", secondClientSecondAuth.endDate)
        assertEquals(mutableListOf(
            Parameter(name="Year of assessment", value="2014")
        ), secondClientSecondAuth.parameters)

    }
}
