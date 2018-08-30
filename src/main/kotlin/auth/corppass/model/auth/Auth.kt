package auth.corppass.model.auth

import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElements
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import java.time.LocalDate
import auth.helper.xml.EmptyStringXmlAdapter

@XmlAccessorType(XmlAccessType.FIELD)
data class Auth (
    @field:XmlElements(
        XmlElement(name = "CPEntID_SUB"),
        XmlElement(name = "CP_ClntEnt_SUB")
    )
    @field:XmlJavaTypeAdapter(value=EmptyStringXmlAdapter::class)
    var subEntityId: String? = null,

    @field:XmlElement(name = "CPRole")
    @field:XmlJavaTypeAdapter(value=EmptyStringXmlAdapter::class)
    var role: String? = null,

    @field:XmlElement(name = "StartDate")
    var startDate: String = "",

    @field:XmlElement(name = "EndDate")
    var endDate: String = "",

    @field:XmlElement(name = "Parameter") var parameters: MutableList<Parameter> = mutableListOf<Parameter>()
) {
    constructor(mapData: Map<String, Any>): this(
        mapData.get("subEntityId") as? String,
        mapData.get("role") as? String,
        mapData.get("startDate") as? String ?: "",
        mapData.get("endDate") as? String ?: "",
        (mapData.get("parameters") as? List<Map<String, String>> ?: listOf<Map<String, String>>()).map {
            parameter -> Parameter(parameter)
        }.toMutableList()
    )

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "subEntityId" to this.subEntityId,
            "role" to this.role,
            "startDate" to this.startDate,
            "endDate" to this.endDate,
            "valid" to this.valid,
            "parameters" to this.parameters.map { parameter -> parameter.toMap() }
        )
    }

    val valid: Boolean
        get() {
            try {
                val currentDate = LocalDate.now()
                val startDate = LocalDate.parse(this.startDate)
                val endDate = LocalDate.parse(this.endDate)
                return startDate.compareTo(currentDate) <= 0 && endDate.compareTo(currentDate) >= 0
            } catch (e: Exception) {
                return false
            }
        }
}
