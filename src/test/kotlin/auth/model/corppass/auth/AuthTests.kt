package auth.model.corppass.auth

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AuthTests {
    val currentDate = LocalDate.now()
    @Test
    fun isValidWhenCurrentDate() {
        val auth = Auth(startDate = currentDate.toString(), endDate = currentDate.toString())
        assertTrue(auth.valid)
    }

    @Test
    fun isValidWhenBetweenDates() {
        val auth = Auth(startDate = currentDate.minusDays(10).toString(), endDate = currentDate.plusDays(10).toString())
        assertTrue(auth.valid)
    }

    @Test
    fun isNotValidWhenPastDates() {
        val auth = Auth(startDate = currentDate.minusDays(10).toString(), endDate = currentDate.minusDays(1).toString())
        assertFalse(auth.valid)
    }

    @Test
    fun isNotValidWhenFutureDates() {
        val auth = Auth(startDate = currentDate.plusDays(1).toString(), endDate = currentDate.plusDays(10).toString())
        assertFalse(auth.valid)
    }
}
