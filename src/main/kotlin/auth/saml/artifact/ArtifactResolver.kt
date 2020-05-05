package auth.saml.artifact

import org.joda.time.DateTime
import auth.helper.Provider
import org.slf4j.LoggerFactory
import auth.saml.util.OpenSAMLUtils
import org.opensaml.saml.saml2.core.Artifact
import org.opensaml.saml.saml2.core.ArtifactResolve
import org.opensaml.saml.saml2.core.ArtifactResponse
import org.opensaml.saml.saml2.core.EncryptedAssertion
import org.opensaml.saml.saml2.core.Issuer
import org.opensaml.saml.saml2.core.Response
import org.opensaml.saml.saml2.core.Assertion
import org.opensaml.saml.saml2.core.Attribute
import com.google.common.base.CaseFormat
import org.opensaml.saml.common.SAMLObject
import org.apache.xml.security.utils.Base64
import org.opensaml.xmlsec.signature.KeyInfo
import javax.servlet.http.HttpServletRequest
import org.opensaml.core.xml.schema.XSString
import auth.saml.credentials.ServiceProviderMetaData
import auth.saml.credentials.IdentityProviderMetaData
import org.opensaml.xmlsec.signature.X509Data
import org.opensaml.soap.common.SOAPException
import org.opensaml.xmlsec.signature.Signature
import org.opensaml.security.credential.Credential
import org.opensaml.saml.saml2.encryption.Decrypter
import org.opensaml.xmlsec.signature.support.Signer
import org.opensaml.xmlsec.signature.X509Certificate
import org.opensaml.messaging.context.MessageContext
import org.opensaml.profile.context.ProfileRequestContext
import org.opensaml.xmlsec.signature.support.SignatureConstants
import org.opensaml.soap.client.http.AbstractPipelineHttpSOAPClient
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder
import org.opensaml.messaging.pipeline.httpclient.HttpClientMessagePipeline
import org.opensaml.messaging.pipeline.httpclient.BasicHttpClientMessagePipeline
import org.opensaml.saml.saml2.binding.decoding.impl.HttpClientResponseSOAP11Decoder
import org.opensaml.saml.saml2.binding.encoding.impl.HttpClientRequestSOAP11Encoder
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver
import org.opensaml.saml.common.SAMLException
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver

object ArtifactResolver {
    private val logger = LoggerFactory.getLogger(ArtifactResolver::class.java)

    fun resolveArtifact(
        artifactId: String,
        request: HttpServletRequest,
        provider: Provider,
        spMetadata: ServiceProviderMetaData,
        idpMetadata: IdentityProviderMetaData
    ): Map<String, Any> {
        try {
            val artifact = buildArtifactFromRequest(artifactId)

            val entityId = spMetadata.entityId
            val artifactResolutionService = idpMetadata.artifactResolutionService
            val artifactResolve = buildArtifactResolve(artifact, entityId, artifactResolutionService)

            signArtifactResolve(artifactResolve, spMetadata.credential)

            logger.debug("Sending ArtifactResolve:")
            OpenSAMLUtils.logSAMLObject(artifactResolve)

            val artifactResponse = sendAndReceiveArtifactResolve(artifactResolve, provider, idpMetadata)
            logger.debug("ArtifactResponse received:")
            OpenSAMLUtils.logSAMLObject(artifactResponse)

            ArtifactValidator.validateDestinationAndLifetime(
                artifactResponse,
                request,
                provider.identityProvider!!.artifactLifetimeClockSkew,
                spMetadata.assertionConsumerService
            )

            val encryptedAssertion = getEncryptedAssertion(artifactResponse)
            val assertion = decryptAssertion(encryptedAssertion, spMetadata.credential)
            logger.debug("Assertion:")
            OpenSAMLUtils.logSAMLObject(assertion)

            ArtifactValidator.verifyAssertionSignature(assertion, idpMetadata.signingCredential)

            val attributes = assertion.attributeStatements[0].attributes
            return convertAttributesToMap(attributes)
        } catch (e: Exception) {
            logger.error("Error in resolving artifact")
            throw e
        }
    }

    internal fun buildArtifactFromRequest(artifactId: String): Artifact {
        val artifactClass = OpenSAMLUtils.buildSAMLObject(Artifact::class.java)
                .orError("Error building SAML object for Artifact")
        artifactClass.artifact = artifactId
        return artifactClass
    }

    internal fun buildArtifactResolve(
        artifact: Artifact,
        entityId: String,
        artifactResolutionService: String
    ): ArtifactResolve {
        val artifactResolve = OpenSAMLUtils.buildSAMLObject(ArtifactResolve::class.java)
                .orError("Error building SAML object for ArtifactResolve")

        val issuer = OpenSAMLUtils.buildSAMLObject(Issuer::class.java)
                .orError("Error building SAML object for Issuer")
        issuer.format = "urn:oasis:names:tc:SAML:2.0:nameidformat:entity"
        issuer.value = entityId

        artifactResolve.issuer = issuer
        artifactResolve.issueInstant = DateTime()
        artifactResolve.id = OpenSAMLUtils.generateSecureRandomId()
        artifactResolve.destination = artifactResolutionService
        artifactResolve.artifact = artifact
        return artifactResolve
    }

    private fun sendAndReceiveArtifactResolve(
        artifactResolve: ArtifactResolve,
        provider: Provider,
        idpMetadata: IdentityProviderMetaData
    ): ArtifactResponse {
        val contextOut = MessageContext<ArtifactResolve>()
        contextOut.message = artifactResolve

        val context = ProfileRequestContext<ArtifactResponse, ArtifactResolve>()
        context.outboundMessageContext = contextOut

        val soapClient = object : AbstractPipelineHttpSOAPClient<SAMLObject, SAMLObject>() {
            @Throws(SOAPException::class)
            override fun newPipeline(): HttpClientMessagePipeline<SAMLObject, SAMLObject> {
                val encoder = HttpClientRequestSOAP11Encoder()
                val decoder = HttpClientResponseSOAP11Decoder()

                return BasicHttpClientMessagePipeline(
                        encoder,
                        decoder
                )
            }
        }
        logger.debug("Context:$context")

        val idp = provider.identityProvider!!
        val clientBuilder = HttpClientBuilder()
        if (idp.artifactServiceProxyHost != null) {
            clientBuilder.connectionProxyHost = idp.artifactServiceProxyHost
        }
        if (idp.artifactServiceProxyPort != null) {
            clientBuilder.connectionProxyPort = idp.artifactServiceProxyPort!!
        }
        if (idp.artifactServiceProxyUsername != null) {
            clientBuilder.connectionProxyUsername = idp.artifactServiceProxyUsername
        }
        if (idp.artifactServiceProxyPassword != null) {
            clientBuilder.connectionProxyPassword = idp.artifactServiceProxyPassword
        }

        soapClient.httpClient = clientBuilder.buildClient()
        soapClient.send(idpMetadata.artifactResolutionService, context)
        if (context.inboundMessageContext?.message == null) {
            throw SAMLException("No SAML response embedded into the artifact response.")
        }
        return context.inboundMessageContext!!.message!!
    }

    internal fun signArtifactResolve(artifactResolve: ArtifactResolve, credential: Credential) {
        val signature = OpenSAMLUtils.buildSAMLObject(Signature::class.java)
                                    .orError("Error building SAML object for Signature")

        signature.signingCredential = credential
        signature.signatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256
        signature.canonicalizationAlgorithm = SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS

        val keyInfo = OpenSAMLUtils.buildSAMLObject(KeyInfo::class.java)
                                    .orError("Error building SAML object for KeyInfo")
        val x509Data = OpenSAMLUtils.buildSAMLObject(X509Data::class.java)
                                    .orError("Error building SAML object for X509Data")
        val x509Cert = OpenSAMLUtils.buildSAMLObject(X509Certificate::class.java)
                                    .orError("Error building SAML object for X509Certificate")

        x509Cert.value = Base64.encode(credential.publicKey?.encoded)
        x509Data.x509Certificates.add(x509Cert)
        keyInfo.x509Datas.add(x509Data)

        signature.keyInfo = keyInfo
        artifactResolve.signature = signature

        XMLObjectProviderRegistrySupport
            .getMarshallerFactory()
            .getMarshaller(artifactResolve)
            ?.marshall(artifactResolve)
        Signer.signObject(signature)
    }

    private fun getEncryptedAssertion(artifactResponse: ArtifactResponse): EncryptedAssertion {
        val response: Response = artifactResponse.message as Response
        val encryptedAssertion: EncryptedAssertion = response.encryptedAssertions[0]
        return encryptedAssertion
    }

    internal fun decryptAssertion(encryptedAssertion: EncryptedAssertion, credential: Credential): Assertion {
        val keyInfoCredentialResolver = StaticKeyInfoCredentialResolver(credential)

        val decrypter = Decrypter(null, keyInfoCredentialResolver, InlineEncryptedKeyResolver())
        decrypter.isRootInNewDocument = true

        return decrypter.decrypt(encryptedAssertion)
    }

    internal fun convertAttributesToMap(attributes: List<Attribute>): Map<String, Any> {
        val result = HashMap<String, Any>()
        for (attribute: Attribute in attributes) {
            val attributeValues = attribute.attributeValues.map {
                attributeValue -> (attributeValue as XSString).value ?: ""
            }
            result.put(
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, attribute.name),
                    if (attributeValues.size > 1) attributeValues else attributeValues.get(0)
            )
        }
        return result
    }

    private fun <T : Any> T?.orError(message: String): T {
        if (this == null) throw IllegalArgumentException(message)
        return this
    }
}
