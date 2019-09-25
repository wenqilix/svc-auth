package auth.plugin

import org.jose4j.jwt.JwtClaims

interface ClaimsMutator {
    fun mutate(claims: JwtClaims): JwtClaims
}

class DefaultClaimsMutator : ClaimsMutator {
    override fun mutate(claims: JwtClaims): JwtClaims = claims
}
