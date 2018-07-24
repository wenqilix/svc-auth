package auth.saml.credentials.resolver

import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver

/**
 * Resolve entityID of each EntityDescriptor in Metadata
 */
class EntityIdResolver(val metadataResolver: AbstractReloadingMetadataResolver) {
    fun resolve(): Iterable<String> {
        return metadataResolver.iterator().asSequence().map { item -> item.entityID }.asIterable()
    }

    fun resolveSingle(): String {
        return resolve().filterNotNull().first()
    }
}
