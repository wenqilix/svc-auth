package auth.model.corppass.mock

import auth.model.corppass.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.web.client.RestTemplate

data class UserList(
    var users: List<User> = emptyList()
) {
    private val logger = LoggerFactory.getLogger(UserList::class.java)

    fun populateFrom(url: String) {
        try {
            val restTemplate = RestTemplate()
            restTemplate.setMessageConverters(listOf(ByteArrayHttpMessageConverter()))
            val response = restTemplate.getForObject(url, ByteArray::class.java)

            val mapper = ObjectMapper()
            users = mapper.readValue(response, UserList::class.java).users
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    fun toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
