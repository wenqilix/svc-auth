package auth.model.singpass

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserTests {
    @Test
    fun `deserialise map to User object with extra property`() {
        val mapOfUser = mapOf("UserName" to "foo", "Mobile" to "bar", "baz" to "qux")
        val mapper = ObjectMapper()
        val user = mapper.convertValue(mapOfUser, User::class.java)

        assertEquals("foo", user.userName)
    }

    @Test
    fun toMap() {
        val user = User("S1234567D", "refresh-token")

        assertEquals("S1234567D", user.toMap()["userName"])
        assertEquals("refresh-token", user.toMap()["refreshToken"])
    }
}
