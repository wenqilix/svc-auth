package auth.saml.credentials

import org.opensaml.core.config.InitializationService
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import org.opensaml.security.credential.Credential
import org.opensaml.security.credential.UsageType
import org.opensaml.security.credential.criteria.impl.EvaluableUsageCredentialCriterion
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver
import org.opensaml.saml.saml2.metadata.impl.ArtifactResolutionServiceImpl
import net.shibboleth.utilities.java.support.resolver.ResolverException
import java.io.File
import auth.saml.credentials.criterion.DefaultCriteriaSet
import auth.saml.credentials.resolver.*
import auth.helper.Properties

open class IdentityProviderMetaData(val path: String, val id: String) {
    private val ENTITY_ID: String
    val ARTIFACT_RESOLUTION_SERVICE: String
    val SIGNING_CREDENTIAL: Credential

    init {
        try {
            InitializationService.initialize()
            val idpMetadataResolver = FilesystemMetadataResolver(File(path))
            idpMetadataResolver.setRequireValidMetadata(true)
            idpMetadataResolver.setParserPool(XMLObjectProviderRegistrySupport.getParserPool()!!)
            idpMetadataResolver.setId(id)
            idpMetadataResolver.initialize()

            ENTITY_ID = EntityIdResolver(idpMetadataResolver).resolveSingle()

            val criteriaSet = DefaultCriteriaSet(ENTITY_ID).buildIdentityProviderCriteria()
            ARTIFACT_RESOLUTION_SERVICE = EndpointResolver(idpMetadataResolver).resolveSingle<ArtifactResolutionServiceImpl>(criteriaSet)

            criteriaSet.add(EvaluableUsageCredentialCriterion(UsageType.SIGNING))
            SIGNING_CREDENTIAL = CredentialResolver(idpMetadataResolver).resolveSingle(criteriaSet)
        } catch (e: ResolverException) {
            throw RuntimeException("Something went wrong reading idp metadata", e)
        }
    }
}

class IdentityProvider {
    object Singpass: IdentityProviderMetaData(
        Properties.getPropertiesContext().singpass!!.identityProvider!!.metadataPath,
        Properties.getPropertiesContext().singpass!!.identityProvider!!.metadataId
    )
    object Corppass: IdentityProviderMetaData(
        Properties.getPropertiesContext().corppass!!.identityProvider!!.metadataPath,
        Properties.getPropertiesContext().corppass!!.identityProvider!!.metadataId
    )
}
