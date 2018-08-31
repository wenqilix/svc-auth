package auth.corppass.model.auth

import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlElement

@XmlAccessorType(XmlAccessType.FIELD)
data class ThirdPartyClient(
    @field:XmlElement(name = "CP_Clnt_ID") var entityId: String = "",
    @field:XmlElement(name = "CP_ClntEnt_TYPE") var entityType: String = "",

    @field:XmlElementWrapper(name = "Auth_Result_Set")
    @field:XmlElement(name = "Row") var auths: MutableList<Auth> = mutableListOf<Auth>()
) {
    constructor(mapData: Map<String, Any>): this(
        mapData.get("entityId") as? String ?: "",
        mapData.get("entityType") as? String ?: "",
        (mapData.get("auths") as? List<Map<String, Any>> ?: listOf<Map<String, Any>>()).map {
            auth -> Auth(auth)
        }.toMutableList()
    )

    fun toMap(): Map<String, Any> {
        return mapOf(
            "entityId" to this.entityId,
            "entityType" to this.entityType,
            "auths" to this.auths.map { auth -> auth.toMap() }
        )
    }
}
