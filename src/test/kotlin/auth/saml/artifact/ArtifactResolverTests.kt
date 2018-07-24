package auth.saml.artifact

import org.junit.Test
import org.junit.Assert.*
import org.hamcrest.Matchers.*
import org.junit.runner.RunWith
import org.opensaml.saml.saml2.core.*
import org.apache.xml.security.utils.Base64
import org.opensaml.core.xml.schema.XSString
import org.opensaml.saml.saml2.core.impl.AttributeBuilder
import org.opensaml.core.xml.schema.impl.XSStringBuilder

class ArtifactResolverUnitTests: ArtifactUnitTestBase() {
    private val artifactId = "artifactId"
    private val entityId = "entityId"
    private val artifactResolutionService = "artifactResolutionService"

    private fun createArtifact() : Artifact {
        return ArtifactResolver.buildArtifactFromRequest(artifactId)
    }

    private fun createArtifactResolve(artifact: Artifact) : ArtifactResolve {
        return ArtifactResolver.buildArtifactResolve(artifact, entityId, artifactResolutionService)
    }

    @Test
    fun buildArtifactFromRequest() {
        val artifact = createArtifact()
        assertNotNull(artifact)
        assertThat(artifact, hasProperty("artifact", `is`(artifactId)))
    }

    @Test
    fun buildArtifactResolve() {
        val artifact = createArtifact()
        val artifactResolve = createArtifactResolve(artifact)
        val issuer = artifactResolve.issuer

        assertNotNull(artifactResolve)
        assertThat(issuer, hasProperty("value", `is`(entityId)))
        assertThat(artifactResolve, hasProperty("destination", `is`(artifactResolutionService)))
        assertThat(artifactResolve, hasProperty("issueInstant"))
        assertThat(artifactResolve, hasProperty("issuer"))
        assertThat(artifactResolve, hasProperty("artifact", `is`(artifact)))
    }

    @Test
    fun signArtifactResolve() {
        val artifact = createArtifact()
        val artifactResolve = createArtifactResolve(artifact)
        ArtifactResolver.signArtifactResolve(artifactResolve, credential)

        val signature = artifactResolve.signature
        val x509Data= signature?.keyInfo?.x509Datas?.get(0)
        val x509Cert = x509Data?.x509Certificates?.get(0)

        assertThat(signature, hasProperty("signingCredential", `is`(credential)))
        assertThat(x509Cert, hasProperty("value", `is`(Base64.encode(publicKey.encoded))))
    }

    @Test
    fun decryptAssertion() {
        // The xml was created using this website: https://www.samltool.com/encrypt.php
        // encrypted with the public key in this file
        val filename = "/ArtifactResolverTest/encryptedResponse.xml"
        val encryptedAssertion = unMarshallElement(filename) as EncryptedAssertion

        val assertion = ArtifactResolver.decryptAssertion(encryptedAssertion,  credential)
        val attributes = assertion.attributeStatements[0].attributes

        val attributesMap = ArtifactResolver.convertAttributesToMap(attributes)
        val attributesMobile = attributesMap["mobile"]
        val attributesUsername = attributesMap["userName"]
        assertEquals(attributesMobile, "94452826")
        assertEquals(attributesUsername, "S3000008J")
    }

    @Test
    fun convertAttributesToMap() {
        val attributeBuilder = AttributeBuilder()
        val stringBuilder = XSStringBuilder()

        val userName: Attribute = attributeBuilder.buildObject()
        userName.name = "UserName"
        val userNameValue: XSString = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME)
        userNameValue.value = "S1234567A"
        userName.attributeValues.add(userNameValue)

        val mobile: Attribute = attributeBuilder.buildObject()
        mobile.name = "Mobile"
        val mobileValue: XSString = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME)
        mobileValue.value = "876543212"
        mobile.attributeValues.add(mobileValue)

        val arrayAttribute: Attribute = attributeBuilder.buildObject()
        arrayAttribute.name = "Array"
        val foo: XSString = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME)
        foo.value = "foo"
        val bar: XSString = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME)
        bar.value = "bar"
        arrayAttribute.attributeValues.add(foo)
        arrayAttribute.attributeValues.add(bar)
				
        val attributes = listOf(userName, mobile, arrayAttribute)
        val result = ArtifactResolver.convertAttributesToMap(attributes)

        assertEquals("S1234567A", result["userName"])
        assertEquals("876543212", result["mobile"])
        assertThat(result["array"] as List<String>, contains("foo", "bar"))
    }
}
