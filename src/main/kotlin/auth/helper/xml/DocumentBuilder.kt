package auth.helper.xml

import javax.xml.bind.JAXBContext
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.util.Base64

class DocumentBuilder {
    companion object {
        inline fun <reified T> parse(inputStream: InputStream): T {
            val jaxbContext = JAXBContext.newInstance(T::class.java)
            val unmarshaller = jaxbContext.createUnmarshaller()
            return unmarshaller.unmarshal(inputStream) as T
        }

        inline fun <reified T> parse(base64Xml: String): T {
            val xml = Base64.getDecoder().decode(base64Xml)
            return parse<T>(ByteArrayInputStream(xml))
        }

        inline fun <reified T> parse(base64Xml: String, rootName: String): T {
            val xml = Base64.getDecoder().decode(base64Xml)
            val sb = StringBuilder()
            sb.append("<$rootName>")
            sb.append(String(xml))
            sb.append("</$rootName>")
            return parse<T>(ByteArrayInputStream(sb.toString().toByteArray()))
        }
    }
}
