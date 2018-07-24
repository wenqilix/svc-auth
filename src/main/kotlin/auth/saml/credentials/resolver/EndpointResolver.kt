package auth.saml.credentials.resolver

import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver
import org.opensaml.saml.metadata.resolver.impl.BasicRoleDescriptorResolver
import org.opensaml.saml.saml2.metadata.impl.IndexedEndpointImpl
import net.shibboleth.utilities.java.support.resolver.CriteriaSet

/**
 * Resolve all ArtifactResolutionService binding location in Metadata from a given criteria set
 */
class EndpointResolver(val metadataResolver: AbstractReloadingMetadataResolver) {
    inline fun <reified T: IndexedEndpointImpl> resolve(criteria: CriteriaSet): Iterable<String> {
        val roleResolver = BasicRoleDescriptorResolver(metadataResolver)
        roleResolver.initialize()

        return roleResolver.resolveSingle(criteria)!!.endpoints
            .filter { endpoint -> endpoint is T }
            .map { endpoint -> endpoint.location }
    }

    inline fun <reified T: IndexedEndpointImpl> resolveSingle(criteria: CriteriaSet): String {
        return resolve<T>(criteria).filterNotNull().first()
    }
}
