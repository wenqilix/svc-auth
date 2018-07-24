package auth.corppass.model

import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.util.Arrays
import auth.helper.xml.DocumentBuilder
import auth.corppass.model.auth.*

class AuthAccessTests {
    @Test
    fun castXMLToAuthAccess() {
        val xml = """
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
                                <Parameter name="other02">value 02</Parameter>
                                <Parameter name="other03">value 03</Parameter>
                                <Parameter name="other04">value 04</Parameter>
                                <Parameter name="other05">value 05</Parameter>
                                <Parameter name="other06">value 06</Parameter>
                                <Parameter name="other07">value 07</Parameter>
                                <Parameter name="other08">value 08</Parameter>
                            </Row>
                        </Auth_Result_Set>
                    </ESrvc_Result>
                </Result_Set>
            </AuthAccess>"""

        val authAccess = DocumentBuilder.parse<AuthAccess>(ByteArrayInputStream(xml.toByteArray()))

        val parameters = mutableListOf(
            Parameter(name="Year of assessment", value="2014"),
            Parameter(name="other02", value="value 02"),
            Parameter(name="other03", value="value 03"),
            Parameter(name="other04", value="value 04"),
            Parameter(name="other05", value="value 05"),
            Parameter(name="other06", value="value 06"),
            Parameter(name="other07", value="value 07"),
            Parameter(name="other08", value="value 08")
        )
        val auths = mutableListOf<Auth>(
            Auth(startDate="2016-01-15", endDate="2016-02-15"),
            Auth(startDate="2016-03-15", endDate="2017-04-15", parameters=parameters)
        )
        
        assertEquals(AuthAccess(auths).toMap(), authAccess.toMap())
    }
}
