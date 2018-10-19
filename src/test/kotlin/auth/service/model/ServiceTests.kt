package auth.service.model

import org.junit.Test
import org.junit.Assert.assertEquals

class ServiceTests {
    @Test
    fun constructor() {
        var mapOfService = mapOf("guid" to "foo123", "publicKey" to "bar", "payload" to mapOf<String, Any>("id" to "*", "scopes" to arrayListOf("foobar:read")))
        var service = Service(mapOfService)
        var payload = mapOf<String, Any>("id" to "*", "scopes" to arrayListOf("foobar:read"))

        assertEquals("foo123", service.guid)
        assertEquals("bar", service.publicKey)
        assertEquals(payload, service.payload)
    }
}
