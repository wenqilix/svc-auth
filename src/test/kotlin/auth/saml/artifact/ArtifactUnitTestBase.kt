package auth.saml.artifact

import org.apache.xml.security.utils.Base64
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import net.shibboleth.utilities.java.support.xml.BasicParserPool
import net.shibboleth.utilities.java.support.xml.XMLParserException
import org.junit.Assert
import org.opensaml.core.config.InitializationService
import org.opensaml.core.xml.XMLObject
import org.opensaml.core.xml.io.UnmarshallingException
import org.opensaml.security.credential.CredentialSupport
import org.opensaml.security.crypto.KeySupport
import java.io.File
import java.io.FileInputStream
import java.security.cert.CertificateFactory

open class ArtifactUnitTestBase {

    private val filePublicKey = readKeyFromFile("keys/public.key")
    private val filePrivateKey = readKeyFromFile("keys/private.key")
    private val fileCertKey = readKeyFromFile("keys/cert.crt")

    protected val publicKey = KeySupport.buildJavaRSAPublicKey(filePublicKey)
    private val privateKey = KeySupport.buildJavaRSAPrivateKey(filePrivateKey)
    private val certFactory = CertificateFactory.getInstance("X.509")
    private val cert = certFactory.generateCertificate(Base64.decode(fileCertKey).inputStream()) as java.security.cert.X509Certificate
    protected val credential = CredentialSupport.getSimpleCredential(cert, privateKey)

    private fun readKeyFromFile(fileName: String): String {
        val classLoader = javaClass.classLoader
        val file = classLoader.getResource(fileName)!!.file
        val filePrivateKey = File(file)
        val fis = FileInputStream(file)
        val encodedPrivateKey = ByteArray(filePrivateKey.length().toInt())
        fis.read(encodedPrivateKey)
        fis.close()
        return String(encodedPrivateKey)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replace("\n", "")
    }

    protected fun unMarshallElement(elementFile: String): XMLObject? {
        try {
            InitializationService.initialize()
            val parser = BasicParserPool()
            parser.initialize()
            val doc = parser.parse(ArtifactUnitTestBase::class.java.getResourceAsStream(elementFile))
            val samlElement = doc.documentElement
            return XMLObjectProviderRegistrySupport.getUnmarshallerFactory().getUnmarshaller(samlElement)?.unmarshall(samlElement)
        } catch ( e: XMLParserException) {
            Assert.fail("Unable to parse element file " + elementFile)
        } catch (e: UnmarshallingException) {
            Assert.fail("Unmarshalling failed when parsing element file " + elementFile + ": " + e)
        }
        return null
    }

}