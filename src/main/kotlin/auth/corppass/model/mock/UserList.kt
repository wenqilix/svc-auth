package auth.corppass.model.mock

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.client.RestTemplate
import org.springframework.http.converter.*
import auth.corppass.model.UserInfo

data class UserList (
    var userInfos: List<UserInfo> = emptyList()
) {
    fun populateFrom(url: String) {
        try {
            val restTemplate = RestTemplate()
            restTemplate.setMessageConverters(listOf(ByteArrayHttpMessageConverter()))
            val response = restTemplate.getForObject(url, ByteArray::class.java)

            val mapper = ObjectMapper()
            userInfos = mapper.readValue(response, auth.corppass.model.mock.UserList::class.java).userInfos
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
