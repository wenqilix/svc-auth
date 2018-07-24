package auth.corppass.model

import javax.xml.bind.annotation.*
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import com.fasterxml.jackson.databind.ObjectMapper
import auth.helper.xml.FlatMapXmlAdapter
import auth.corppass.model.auth.Auth

@XmlRootElement(name = "AuthAccess")
@XmlAccessorType(XmlAccessType.FIELD)
data class AuthAccess (
    @field:XmlJavaTypeAdapter(value=FlatMapXmlAdapter::class)
    @field:XmlElement(name = "Result_Set")
    var auths: MutableList<Auth> = mutableListOf<Auth>()
) {
    constructor(mapData: Map<String, Any>): this(
        (mapData.get("auths") as? List<Map<String, Any>> ?: listOf<Map<String, Any>>()).map {
            auth -> Auth(auth)
        }.toMutableList()
    )

    fun toMap(): Map<String, Any> {
        return mapOf(
            "auths" to this.auths.map { auth -> auth.toMap() }
        )
    }

    fun toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
