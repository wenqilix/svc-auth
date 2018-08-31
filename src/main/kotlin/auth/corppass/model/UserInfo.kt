package auth.corppass.model

import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import auth.helper.xml.BooleanStringXmlAdapter
import auth.helper.xml.EmptyStringXmlAdapter

@XmlRootElement(name = "UserInfo")
@XmlAccessorType(XmlAccessType.FIELD)
data class UserInfo(
    @field:XmlElement(name = "CPAccType") var accountType: String = "User",
    @field:XmlElement(name = "CPUID") var userId: String = "",
    @field:XmlElement(name = "CPUID_Country") var userCountry: String = "SG",
    @field:XmlElement(name = "CPUID_FullName") var userFullName: String = "",
    @field:XmlElement(name = "CPSystemUID") var userName: String = "",

    @field:XmlElement(name = "ISSPHOLDER")
    @field:XmlJavaTypeAdapter(type = Boolean::class, value = BooleanStringXmlAdapter::class)
    var singpassHolder: Boolean = false,

    @field:XmlElement(name = "CPEntID") var entityId: String = "",

    @field:XmlElement(name = "CPEnt_Status")
    @field:XmlJavaTypeAdapter(value = EmptyStringXmlAdapter::class)
    var entityStatus: String? = null,

    @field:XmlElement(name = "CPEnt_TYPE") var entityType: String = "UEN",

    @field:XmlElement(name = "CPNonUEN_RegNo")
    @field:XmlJavaTypeAdapter(value = EmptyStringXmlAdapter::class)
    var entityRegNo: String? = null,

    @field:XmlElement(name = "CPNonUEN_Country")
    @field:XmlJavaTypeAdapter(value = EmptyStringXmlAdapter::class)
    var entityCountry: String? = null,

    @field:XmlElement(name = "CPNonUEN_Name")
    @field:XmlJavaTypeAdapter(value = EmptyStringXmlAdapter::class)
    var entityName: String? = null
) {
    constructor(mapData: Map<String, Any>): this(
        mapData.get("accountType") as? String ?: "",
        mapData.get("userId") as? String ?: "",
        mapData.get("userCountry") as? String ?: "",
        mapData.get("userFullName") as? String ?: "",
        mapData.get("userName") as? String ?: "",
        mapData.get("singpassHolder") as? Boolean ?: false,
        mapData.get("entityId") as? String ?: "",
        mapData.get("entityStatus") as? String,
        mapData.get("entityType") as? String ?: "",
        mapData.get("entityRegNo") as? String,
        mapData.get("entityCountry") as? String,
        mapData.get("entityName") as? String
    )

    fun toMap(): Map<String, Any?> {
        val mapper = ObjectMapper()
        return mapper.convertValue(this, object : TypeReference<Map<String, Any?>>() {})
    }

    fun toUrlParams(): String {
        val paramList: List<String> = this.toMap().map { ele -> "${ele.key}=${ele.value ?: ""}" }
        return paramList.joinToString("&")
    }

    fun toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
