package auth.saml.util

import org.w3c.dom.Element
import java.io.StringWriter
import org.slf4j.LoggerFactory
import javax.xml.namespace.QName
import javax.xml.transform.OutputKeys
import org.opensaml.core.xml.XMLObject
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.TransformerFactory
import java.security.NoSuchAlgorithmException
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.TransformerException
import org.opensaml.saml.common.SignableSAMLObject
import org.opensaml.core.xml.io.MarshallingException
import org.opensaml.core.config.InitializationService
import javax.xml.transform.TransformerConfigurationException
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import net.shibboleth.utilities.java.support.security.RandomIdentifierGenerationStrategy

object OpenSAMLUtils {
    private val logger = LoggerFactory.getLogger(OpenSAMLUtils::class.java)
    private var secureRandomIdGenerator: RandomIdentifierGenerationStrategy? = null

    init {
        try {
            InitializationService.initialize()
            secureRandomIdGenerator = RandomIdentifierGenerationStrategy()
        } catch (e: NoSuchAlgorithmException) {
            logger.error(e.message, e)
        }
    }

    fun <T> buildSAMLObject(clazz: Class<T>): T? {
        try {
            val builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory()
            val defaultElementName = clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null) as QName
            return builderFactory.getBuilder(defaultElementName)?.buildObject(defaultElementName) as T
        } catch (e: IllegalAccessException) {
            throw IllegalArgumentException("Could not build SAML object")
        } catch (e: NoSuchFieldException) {
            throw IllegalArgumentException("Could not build SAML object")
        }
    }

    fun generateSecureRandomId(): String {
        return secureRandomIdGenerator!!.generateIdentifier()
    }

    fun logSAMLObject(samlObj: XMLObject) {
        var element: Element? = null

        if (samlObj is SignableSAMLObject && samlObj.isSigned && samlObj.getDOM() != null) {
            element = samlObj.getDOM()
        } else {
            try {
                val out = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(samlObj)
                out?.marshall(samlObj)
                element = samlObj.dom
            } catch (e: MarshallingException) {
                logger.error(e.message, e)
            }
        }

        try {
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            val result = StreamResult(StringWriter())
            val source = DOMSource(element)

            transformer.transform(source, result)
            val xmlString = result.writer.toString()

            logger.debug(xmlString)
        } catch (e: TransformerConfigurationException) {
            e.printStackTrace()
        } catch (e: TransformerException) {
            e.printStackTrace()
        }

    }
}
