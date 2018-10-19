package auth.service.model

import org.junit.Test
import org.junit.Assert.assertEquals

class PayloadTests {
    @Test
    fun toMap() {
        var payload = Payload(mapOf<String, Any>("foo" to "bar"))

        assertEquals(mapOf<String, Any>("foo" to "bar"), payload.toMap()["payload"])
    }
}
