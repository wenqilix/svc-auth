package auth.model.corppass.auth

import auth.util.helper.json.EmptyStringJsonDeserializer
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDate

data class Auth(
    @JsonSetter("CPEntID_SUB")
    @JsonAlias("CP_ClntEnt_SUB")
    @JsonDeserialize(using = EmptyStringJsonDeserializer::class)
    var subEntityId: String? = null,

    @JsonSetter("CPRole")
    @JsonDeserialize(using = EmptyStringJsonDeserializer::class)
    var role: String? = null,

    @JsonSetter("StartDate", nulls = Nulls.AS_EMPTY)
    var startDate: String = "",

    @JsonSetter("EndDate", nulls = Nulls.AS_EMPTY)
    var endDate: String = "",

    @JsonSetter("Parameter", nulls = Nulls.AS_EMPTY)
    var parameters: MutableList<Parameter> = mutableListOf<Parameter>()
) {
    constructor(mapData: Map<String, Any>) : this(
        mapData.get("subEntityId") as? String,
        mapData.get("role") as? String,
        mapData.get("startDate") as? String ?: "",
        mapData.get("endDate") as? String ?: "",
        (mapData.get("parameters") as? List<Map<String, String>> ?: listOf<Map<String, String>>()).map {
            parameter ->
            Parameter(parameter)
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

    @get:JsonIgnore
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
