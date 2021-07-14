package auth.util.helper.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class EmptyStringJsonDeserializer : JsonDeserializer<String?>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): String? {
        val value = parser.getText()
        if (value == "NULL" || value == "") {
            return null
        }
        return value
    }
}
