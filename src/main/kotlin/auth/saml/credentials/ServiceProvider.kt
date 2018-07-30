package auth.saml.credentials

import org.opensaml.core.config.InitializationService
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import org.opensaml.security.credential.Credential
import org.opensaml.security.credential.BasicCredential
import org.opensaml.security.credential.UsageType
import org.opensaml.security.credential.criteria.impl.EvaluableUsageCredentialCriterion
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceImpl
import net.shibboleth.utilities.java.support.resolver.ResolverException
import java.io.File
import auth.saml.credentials.criterion.DefaultCriteriaSet
import auth.saml.credentials.resolver.*
import auth.helper.Cryptography
import auth.helper.Properties

open class ServiceProviderMetaData(val path: String, val id: String) {
    val ENTITY_ID: String
    val ASSERTION_CONSUMER_SERVICE: String
    val CREDENTIAL: Credential

    init {
        try {
            InitializationService.initialize()
            val spMetadataResolver = FilesystemMetadataResolver(File(path))
            spMetadataResolver.setRequireValidMetadata(true)
            spMetadataResolver.setParserPool(XMLObjectProviderRegistrySupport.getParserPool()!!)
            spMetadataResolver.setId(id)
            spMetadataResolver.initialize()

            ENTITY_ID = EntityIdResolver(spMetadataResolver).resolveSingle()

            val criteriaSet = DefaultCriteriaSet(ENTITY_ID).buildServiceProviderCriteria()
            ASSERTION_CONSUMER_SERVICE = EndpointResolver(spMetadataResolver).resolveSingle<AssertionConsumerServiceImpl>(criteriaSet)

            criteriaSet.add(EvaluableUsageCredentialCriterion(UsageType.ENCRYPTION))
            CREDENTIAL = CredentialResolver(spMetadataResolver).resolveSingle(criteriaSet)

            val properties = Properties.getPropertiesContext()
            val key = Cryptography.generatePrivate(properties.singpass!!.serviceProvider!!.privateKey)
            (CREDENTIAL as BasicCredential).setPrivateKey(key)
        } catch (e: ResolverException) {
            throw RuntimeException("Something went wrong reading sp metadata/credential", e)
        }
    }
}

class ServiceProvider {
    object Singpass: ServiceProviderMetaData(
        Properties.getPropertiesContext().singpass!!.serviceProvider!!.metadataPath,
        Properties.getPropertiesContext().singpass!!.serviceProvider!!.metadataId
    )
    object Corppass: ServiceProviderMetaData(
        Properties.getPropertiesContext().corppass!!.serviceProvider!!.metadataPath,
        Properties.getPropertiesContext().corppass!!.serviceProvider!!.metadataId
    )
}