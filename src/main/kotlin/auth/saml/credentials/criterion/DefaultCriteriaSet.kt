package auth.saml.credentials.criterion

import net.shibboleth.utilities.java.support.resolver.CriteriaSet
import org.opensaml.core.criterion.EntityIdCriterion
import org.opensaml.saml.criterion.EntityRoleCriterion
import org.opensaml.saml.saml2.metadata.SPSSODescriptor
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor

/**
 * Criteria must containe either
 * RoleDescriptorCriterion or EntityIdCriterion + EntityRoleCriterion
 * to perform resolution
 */
class DefaultCriteriaSet {
    private val criteria = CriteriaSet()
    constructor(entityId: String) {
        criteria.add(EntityIdCriterion(entityId))
    }

    fun buildServiceProviderCriteria(): CriteriaSet {
        this.criteria.add(EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME))
        return this.criteria
    }

    fun buildIdentityProviderCriteria(): CriteriaSet {
        this.criteria.add(EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME))
        return this.criteria
    }
}
