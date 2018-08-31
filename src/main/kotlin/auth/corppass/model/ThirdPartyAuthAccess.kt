package auth.corppass.model

import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import com.fasterxml.jackson.databind.ObjectMapper
import auth.helper.xml.EmptyStringXmlAdapter
import auth.helper.xml.FlatMapXmlAdapter
import auth.corppass.model.auth.ThirdPartyClient

@XmlRootElement(name = "TPAuthAccess")
@XmlAccessorType(XmlAccessType.FIELD)
data class ThirdPartyAuthAccess(
    @field:XmlElement(name = "CP_TPEntID") var entityId: String = "",

    @field:XmlJavaTypeAdapter(value = EmptyStringXmlAdapter::class)
    @field:XmlElement(name = "CP_TPEnt_Status") var entityStatus: String? = null,

    @field:XmlElement(name = "CP_TPEnt_TYPE") var entityType: String = "",

    @field:XmlJavaTypeAdapter(value = FlatMapXmlAdapter::class)
    @field:XmlElement(name = "Result_Set")
    var clients: MutableList<ThirdPartyClient> = mutableListOf<ThirdPartyClient>()
) {
    constructor(mapData: Map<String, Any>): this(
        mapData.get("entityId") as? String ?: "",
        mapData.get("entityStatus") as? String,
        mapData.get("entityType") as? String ?: "",
        (mapData.get("clients") as? List<Map<String, Any>> ?: listOf<Map<String, Any>>()).map {
            client -> ThirdPartyClient(client)
        }.toMutableList()
    )

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "entityId" to this.entityId,
            "entityStatus" to this.entityStatus,
            "entityType" to this.entityType,
            "clients" to this.clients.map { client -> client.toMap() }
        )
    }

    fun toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
