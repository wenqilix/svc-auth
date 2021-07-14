package auth.util.helper.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class BooleanStringJsonDeserializer : JsonDeserializer<Boolean>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Boolean {
        return parser.getText() == "YES"
    }
}
