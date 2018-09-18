package auth.corppass.model.mock

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.client.RestTemplate
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import auth.corppass.model.User

data class UserList(
    var users: List<User> = emptyList()
) {
    fun populateFrom(url: String) {
        try {
            val restTemplate = RestTemplate()
            restTemplate.setMessageConverters(listOf(ByteArrayHttpMessageConverter()))
            val response = restTemplate.getForObject(url, ByteArray::class.java)

            val mapper = ObjectMapper()
            users = mapper.readValue(response, UserList::class.java).users
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
