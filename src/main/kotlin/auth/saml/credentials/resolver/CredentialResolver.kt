package auth.saml.credentials.resolver

import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver
import org.opensaml.saml.metadata.resolver.impl.BasicRoleDescriptorResolver
import org.opensaml.saml.security.impl.MetadataCredentialResolver
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap
import net.shibboleth.utilities.java.support.resolver.CriteriaSet
import org.opensaml.security.credential.Credential

/**
 * Resolve all Credential Certificates in Metadata from a given criteria set
 */
class CredentialResolver(val metadataResolver: AbstractReloadingMetadataResolver) {
    fun resolve(criteria: CriteriaSet): Iterable<Credential> {
        val keyResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver()
        val roleResolver = BasicRoleDescriptorResolver(metadataResolver)

        val resolver = MetadataCredentialResolver()
        resolver.setRoleDescriptorResolver(roleResolver)
        resolver.setKeyInfoCredentialResolver(keyResolver)
        resolver.initialize()
        roleResolver.initialize()

        return resolver.resolve(criteria)
    }

    fun resolveSingle(criteria: CriteriaSet): Credential {
        return resolve(criteria).first()
    }
}
