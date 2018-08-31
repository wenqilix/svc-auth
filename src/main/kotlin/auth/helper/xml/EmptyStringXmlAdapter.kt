package auth.helper.xml

import javax.xml.bind.annotation.adapters.XmlAdapter

class EmptyStringXmlAdapter : XmlAdapter<String, String?>() {
    override fun unmarshal(v: String): String? {
        if (v == "NULL" || v == "") {
            return null
        }
        return v
    }

    override fun marshal(v: String?): String {
        return v ?: ""
    }
}
