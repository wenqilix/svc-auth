package auth.singpass.model

import org.junit.Test
import org.junit.Assert.*

class UserTests {
    @Test
    fun constructor() {
        var mapOfUser = mapOf("userName" to "foo", "mobile" to "bar", "baz" to "qux")
        var user = User(mapOfUser)

        assertEquals("foo", user.userName)
        assertEquals("bar", user.mobile)
    }
    @Test
    fun toMap() {
        val user = User("S1234567D", "99999999")

        assertEquals("S1234567D", user.toMap()["userName"])
        assertEquals("99999999", user.toMap()["mobile"])
    }
}
