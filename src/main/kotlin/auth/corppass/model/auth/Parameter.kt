package auth.corppass.model.auth

import javax.xml.bind.annotation.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference

@XmlAccessorType(XmlAccessType.FIELD)
data class Parameter (
    @field:XmlValue var value: String = "",
    @field:XmlAttribute(name = "name") var name: String = ""
) {
    constructor(mapData: Map<String, String>): this(
        mapData.getOrDefault("value", ""),
        mapData.getOrDefault("name", "")
    )

    fun toMap(): Map<String, String> {
        val mapper = ObjectMapper()
        return mapper.convertValue(this, object: TypeReference<Map<String, String>>() {})
    }
}
