package auth.helper.xml

import javax.xml.bind.annotation.adapters.XmlAdapter

class BooleanStringXmlAdapter : XmlAdapter<String, Boolean>() {
    override fun unmarshal(v: String): Boolean {
        return v == "YES"
    }

    override fun marshal(v: Boolean): String {
        if (v) {
            return "YES"
        } else {
            return "NO"
        }
    }
}
