package auth.util.helper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

class AdditionalInfoRequest(val restTemplate: RestTemplate = RestTemplate()) {
    private val logger = LoggerFactory.getLogger(AdditionalInfoRequest::class.java)
    var url: String? = null
    var httpMethod: String = "GET"
    var body: String? = null
    var staticJson: String? = null
    var requestTimeout: Int = 0

    private fun getClientHttpRequestFactory(): SimpleClientHttpRequestFactory {
        val clientHttpRequestFactory = SimpleClientHttpRequestFactory()
        clientHttpRequestFactory.setConnectTimeout(this.requestTimeout)
        clientHttpRequestFactory.setReadTimeout(this.requestTimeout)
        return clientHttpRequestFactory
    }

    private val static: Map<String, Any>? by lazy {
        var staticMap: Map<String, Any>?
        if (this.staticJson == null) {
            staticMap = null
        } else {
            val mapper = ObjectMapper()
            staticMap = mapper.readValue(this.staticJson, object : TypeReference<Map<String, Any>>() {})
        }
        staticMap
    }

    fun resolveAdditionalInfo(headers: Map<String, String>? = null): Map<String, Any>? {
        this.url?.let {
            try {
                val httpHeaders = HttpHeaders()
                headers?.let { httpHeaders.setAll(it) }
                httpHeaders.setContentType(MediaType.APPLICATION_JSON)
                restTemplate.setRequestFactory(getClientHttpRequestFactory())
                val request = HttpEntity<String>(this.body, httpHeaders)
                val respType = object : ParameterizedTypeReference<Map<String, Any>>() {}
                val response = restTemplate.exchange(
                    it,
                    HttpMethod.valueOf(this.httpMethod),
                    request,
                    respType
                )

                val responseBody = response.getBody()
                if (responseBody?.get("errors") != null) { // handle graphql errors
                    logger.error("Error while fetching additional info: {}", responseBody)
                    throw AdditionalInfoRequestException("Error while fetching additional info")
                }
                return responseBody
            } catch (e: Exception) {
                logger.error("Error in additional info request", e)
            }
        }
        return this.static
    }
}

class AdditionalInfoRequestException(message: String) : Exception(message)
