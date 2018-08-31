package auth.corppass.model.auth

import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlElement

@XmlAccessorType(XmlAccessType.FIELD)
data class ResultSet(
    @field:XmlElement(name = "ESrvc_Result")
    var results: MutableList<Result> = mutableListOf<Result>()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Result(
    @field:XmlElementWrapper(name = "Auth_Result_Set")
    @field:XmlElement(name = "Row") var auths: MutableList<Auth>? = null,

    @field:XmlElementWrapper(name = "Auth_Set")
    @field:XmlElement(name = "TP_Auth") var clients: MutableList<ThirdPartyClient>? = null
)
