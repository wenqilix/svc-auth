package auth.service.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference

data class Payload(
    var payload: Map<String, Any>
) {
    fun toMap(): Map<String, Any> {
        val mapper = ObjectMapper()
        return mapper.convertValue(this, object : TypeReference<Map<String, Any>>() {})
    }
}
