package auth.saml.artifact

import org.slf4j.LoggerFactory
import org.opensaml.messaging.context.MessageContext
import javax.servlet.http.HttpServletRequest
import org.opensaml.messaging.handler.MessageHandler
import org.opensaml.messaging.handler.impl.BasicMessageHandlerChain
import org.opensaml.saml.common.binding.security.impl.MessageLifetimeSecurityHandler
import org.opensaml.saml.common.binding.security.impl.ReceivedEndpointSecurityHandler
import org.opensaml.saml.common.SAMLException
import org.opensaml.saml.saml2.core.ArtifactResponse
import org.opensaml.saml.saml2.core.Response
import org.opensaml.saml.saml2.core.Assertion
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator
import org.opensaml.xmlsec.signature.support.SignatureValidator
import org.opensaml.security.credential.Credential
import auth.helper.BasicDestinationURLComparator

object ArtifactValidator {
    const val MESSAGE_LIFETIME: Long = 60000
    private val logger = LoggerFactory.getLogger(ArtifactValidator::class.java)

    fun validateDestinationAndLifetime(
        artifactResponse: ArtifactResponse,
        request: HttpServletRequest,
        lifetimeClockSkew: Long = 0,
        callbackUrl: String? = null
    ) {
        val context = MessageContext<Any>()
        val response = artifactResponse.message as? Response
        context.message = response

        val lifetimeSecurityHandler = MessageLifetimeSecurityHandler()
        lifetimeSecurityHandler.clockSkew = lifetimeClockSkew
        lifetimeSecurityHandler.messageLifetime = MESSAGE_LIFETIME
        lifetimeSecurityHandler.isRequiredRule = true

        val receivedEndpointSecurityHandler = ReceivedEndpointSecurityHandler()
        receivedEndpointSecurityHandler.httpServletRequest = request
        receivedEndpointSecurityHandler.uriComparator = BasicDestinationURLComparator(callbackUrl)

        val handlers: List<MessageHandler<Any>> = listOf(lifetimeSecurityHandler, receivedEndpointSecurityHandler)
        val handlerChain = BasicMessageHandlerChain<Any>()
        handlerChain.handlers = handlers

        handlerChain.initialize()
        handlerChain.doInvoke(context)
    }

    fun verifyAssertionSignature(assertion: Assertion, credential: Credential) {
        if (!assertion.isSigned) throw SAMLException("The SAML Assertion was not signed")

        val profileValidator = SAMLSignatureProfileValidator()
        val signature = assertion.signature.orError("Error getting assertion signature")

        profileValidator.validate(signature)
        SignatureValidator.validate(signature, credential)
        logger.debug("SAML Assertion signature verified")
    }

    private fun <T : Any> T?.orError(message: String): T {
        if (this == null) throw IllegalArgumentException(message)
        return this
    }
}
