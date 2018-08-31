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
import auth.saml.credentials.resolver.EntityIdResolver
import auth.saml.credentials.resolver.EndpointResolver
import auth.saml.credentials.resolver.CredentialResolver
import auth.helper.Properties

open class IdentityProviderMetaData(val path: String, val id: String) {
    private val _entityId: String
    val artifactResolutionService: String
    val signingCredential: Credential

    init {
        try {
            InitializationService.initialize()
            val idpMetadataResolver = FilesystemMetadataResolver(File(path))
            idpMetadataResolver.setRequireValidMetadata(true)
            idpMetadataResolver.setParserPool(XMLObjectProviderRegistrySupport.getParserPool()!!)
            idpMetadataResolver.setId(id)
            idpMetadataResolver.initialize()

            _entityId = EntityIdResolver(idpMetadataResolver).resolveSingle()

            val criteriaSet = DefaultCriteriaSet(_entityId).buildIdentityProviderCriteria()
            artifactResolutionService =
                EndpointResolver(idpMetadataResolver).resolveSingle<ArtifactResolutionServiceImpl>(criteriaSet)

            criteriaSet.add(EvaluableUsageCredentialCriterion(UsageType.SIGNING))
            signingCredential = CredentialResolver(idpMetadataResolver).resolveSingle(criteriaSet)
        } catch (e: ResolverException) {
            throw RuntimeException("Something went wrong reading idp metadata", e)
        }
    }
}

class IdentityProvider {
    object Singpass : IdentityProviderMetaData(
        Properties.getPropertiesContext().singpass!!.identityProvider!!.metadataPath,
        Properties.getPropertiesContext().singpass!!.identityProvider!!.metadataId
    )
    object Corppass : IdentityProviderMetaData(
        Properties.getPropertiesContext().corppass!!.identityProvider!!.metadataPath,
        Properties.getPropertiesContext().corppass!!.identityProvider!!.metadataId
    )
}
