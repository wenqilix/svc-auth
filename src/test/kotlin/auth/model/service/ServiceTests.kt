package auth.model.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServiceTests {
    @Test
    fun constructor() {
        val mapOfService = mapOf("guid" to "foo123", "publicKey" to "bar", "payload" to mapOf<String, Any>("id" to "*", "scopes" to arrayListOf("foobar:read")))
        val service = Service(mapOfService)
        val payload = mapOf<String, Any>("id" to "*", "scopes" to arrayListOf("foobar:read"))

        assertEquals("foo123", service.guid)
        assertEquals("bar", service.publicKey)
        assertEquals(payload, service.payload)
    }
}
