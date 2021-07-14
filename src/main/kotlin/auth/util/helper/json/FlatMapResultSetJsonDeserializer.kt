package auth.util.helper.json

import auth.model.corppass.auth.Auth
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.ContextualDeserializer

class FlatMapResultSetJsonDeserializer<T> : JsonDeserializer<List<T>>, ContextualDeserializer {
    private var type: JavaType?
    constructor() {
        this.type = null
    }
    constructor(type: JavaType) {
        this.type = type
    }

    override fun createContextual(
        deserializationContext: DeserializationContext,
        beanProperty: BeanProperty
    ): JsonDeserializer<List<T>> {
        // beanProperty is null when the type to deserialize is the top-level type or a generic type, not a type of a bean property
        val type: JavaType = if (deserializationContext.getContextualType() != null) {
            deserializationContext.getContextualType()
        } else {
            beanProperty.getMember().getType()
        }
        return FlatMapResultSetJsonDeserializer(type)
    }

    override fun deserialize(parser: JsonParser, context: DeserializationContext): List<T> {
        val node: JsonNode = parser.readValueAsTree()
        return getAuthRows(node).flatMap {
            val authParser = it.traverse(parser.codec)
            authParser.nextToken()
            context.readValue(authParser, this.type)
        }
    }

    fun getAuthRows(node: JsonNode): List<JsonNode> {
        return if (node.has("Row")) {
            listOf(node.get("Row"))
        } else if (node.has("ESrvc_Result")) {
            if (this.type?.getContentType()?.isTypeOrSubTypeOf(Auth::class.java) == true) {
                node.findValues("Row")
            } else {
                node.findValues("TP_Auth")
            }
        } else {
            listOf(node)
        }
    }
}
