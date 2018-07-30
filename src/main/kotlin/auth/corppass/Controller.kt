package auth.corppass

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import auth.helper.Properties
import auth.helper.Jwt
import auth.saml.artifact.ArtifactResolver
import auth.saml.credentials.ServiceProvider
import auth.saml.credentials.IdentityProvider
import auth.helper.xml.DocumentBuilder
import auth.corppass.model.*
import auth.corppass.model.auth.*
import auth.corppass.model.mock.UserList

@Controller("corppassController")
@RequestMapping("/cp")
@Profile("qa", "development", "remote-development")
class DevelopmentController {
    @Autowired
    lateinit var properties: Properties
    @Autowired
    lateinit var jwt: Jwt

    @GetMapping("/login")
    fun login(model: Model, @RequestParam(value="target", required=false) target: String?): String {
        val mockUserList = UserList()
        if (properties.corppass!!.mockUserListUrl != null) {
            mockUserList.populateFrom(properties.corppass!!.mockUserListUrl!!)
        }
        model.addAttribute("userList", mockUserList)
        model.addAttribute("userInfo", UserInfo())
        model.addAttribute("authAccess", AuthAccess())
        model.addAttribute("thirdPartyAuthAccess", ThirdPartyAuthAccess())
        model.addAttribute("auth", Auth())
        model.addAttribute("target", target ?: properties.corppass!!.homepageUrl)

        return "corppass"
    }

    @PostMapping("/cb")
    fun callback(
        userInfo: UserInfo, model: Model,
        @RequestParam(value="RelayState") relayState: String,
        @RequestParam(value="authAccessState") authAccessState: String,
        @RequestParam(value="thirdPartyAuthAccessState") thirdPartyAuthAccessState: String
    ): String {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val authAccess = mapper.readValue(authAccessState, AuthAccess::class.java)
        val thirdPartyAuthAccess = mapper.readValue(thirdPartyAuthAccessState, ThirdPartyAuthAccess::class.java)

        if (userInfo.entityType == "UEN") {
            userInfo.entityRegNo = null
            userInfo.entityCountry = null
            userInfo.entityName = null
        } else {
            userInfo.entityStatus = null
        }
        thirdPartyAuthAccess.entityId = userInfo.entityId
        thirdPartyAuthAccess.entityStatus = userInfo.entityStatus
        thirdPartyAuthAccess.entityType = userInfo.entityType

        val token: String = jwt.buildCorppass(User(userInfo, authAccess, thirdPartyAuthAccess).toMap())

        model.addAttribute("model", object {
            val postUrl: String = properties.corppass!!.serviceProvider!!.loginUrl
            val token: String = token
            val relayState: String = relayState
        })

        return "relay"
    }

    @PostMapping("/role", params=["add"])
    fun addRole(
        auth: Auth, model: Model,
        @RequestParam(value="thirdParty", required=false) thirdParty: Boolean?,
        @RequestParam(value="clientEntityId") clientEntityId: String,
        @RequestParam(value="clientEntityType") clientEntityType: String,
        @RequestParam(value="target") target: String,
        @RequestParam(value="userInfoState") userInfoState: String,
        @RequestParam(value="authAccessState") authAccessState: String,
        @RequestParam(value="thirdPartyAuthAccessState") thirdPartyAuthAccessState: String,
        @RequestParam(value="userListState") userListState: String
    ): String {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val userInfo = mapper.readValue(userInfoState, UserInfo::class.java)
        val authAccess = mapper.readValue(authAccessState, AuthAccess::class.java)
        val thirdPartyAuthAccess = mapper.readValue(thirdPartyAuthAccessState, ThirdPartyAuthAccess::class.java)
        val userList = mapper.readValue(userListState, UserList::class.java)

        if (thirdParty != null && thirdParty) {
            val clientIndex = thirdPartyAuthAccess.clients.indexOfFirst {
                client -> client.entityId == clientEntityId && client.entityType == clientEntityType
            }
            if (clientIndex < 0) {
                val thirdPartyClient = ThirdPartyClient(clientEntityId, clientEntityType)
                thirdPartyClient.auths.add(auth)
                thirdPartyAuthAccess.clients.add(thirdPartyClient)
            } else {
                thirdPartyAuthAccess.clients.get(clientIndex).auths.add(auth)
            }
        } else {
            authAccess.auths.add(auth)
        }

        model.addAttribute("userList", userList)
        model.addAttribute("userInfo", userInfo)
        model.addAttribute("authAccess", authAccess)
        model.addAttribute("thirdPartyAuthAccess", thirdPartyAuthAccess)
        model.addAttribute("auth", Auth())
        model.addAttribute("target", target)

        return "corppass"
    }

    @PostMapping("/role", params=["remove"])
    fun removeRole(
        model: Model,
        @RequestParam(value="remove") index: Int,
        @RequestParam(value="target") target: String,
        @RequestParam(value="userInfoState") userInfoState: String,
        @RequestParam(value="authAccessState") authAccessState: String,
        @RequestParam(value="thirdPartyAuthAccessState") thirdPartyAuthAccessState: String,
        @RequestParam(value="userListState") userListState: String
    ): String {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val userInfo = mapper.readValue(userInfoState, UserInfo::class.java)
        val authAccess = mapper.readValue(authAccessState, AuthAccess::class.java)
        val thirdPartyAuthAccess = mapper.readValue(thirdPartyAuthAccessState, ThirdPartyAuthAccess::class.java)
        val userList = mapper.readValue(userListState, UserList::class.java)

        var auths = authAccess.auths
        if (index >= 1000) {
            val clientIndex = (index/1000) - 1
            auths = thirdPartyAuthAccess.clients.get(clientIndex).auths
        }
        auths.removeAt(index % 1000)

        model.addAttribute("userList", userList)
        model.addAttribute("userInfo", userInfo)
        model.addAttribute("authAccess", authAccess)
        model.addAttribute("thirdPartyAuthAccess", thirdPartyAuthAccess)
        model.addAttribute("auth", Auth())
        model.addAttribute("target", target)

        return "corppass"
    }

    @PostMapping("/userInfo", params=["select"])
    fun removeRole(
        model: Model,
        @RequestParam(value="select") index: Int,
        @RequestParam(value="target") target: String,
        @RequestParam(value="authAccessState") authAccessState: String,
        @RequestParam(value="thirdPartyAuthAccessState") thirdPartyAuthAccessState: String,
        @RequestParam(value="userListState") userListState: String
    ): String {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val authAccess = mapper.readValue(authAccessState, AuthAccess::class.java)
        val thirdPartyAuthAccess = mapper.readValue(thirdPartyAuthAccessState, ThirdPartyAuthAccess::class.java)
        val userList = mapper.readValue(userListState, UserList::class.java)

        val userInfo = userList.userInfos.get(index)

        model.addAttribute("userList", userList)
        model.addAttribute("userInfo", userInfo)
        model.addAttribute("authAccess", authAccess)
        model.addAttribute("thirdPartyAuthAccess", thirdPartyAuthAccess)
        model.addAttribute("auth", Auth())
        model.addAttribute("target", target)

        return "corppass"
    }
}

@Controller("corppassController")
@RequestMapping("/cp")
@Profile("uat", "production")
class ProductionController {
    @Autowired
    lateinit var properties: Properties
    @Autowired
    lateinit var jwt: Jwt

    @GetMapping("/login")
    fun login(@RequestParam(value="target", required=false) target: String?): String {
        val idp = properties.corppass!!.identityProvider!!
        val uriComponents: UriComponents = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(idp.host)
            .path("/FIM/sps/CorpIDPFed/saml20/logininitial")
            .queryParam("RequestBinding", "HTTPArtifact")
            .queryParam("ResponseBinding", "HTTPArtifact")
            .queryParam("PartnerId", ServiceProvider.Corppass.ENTITY_ID)
            .queryParam("Target", target ?: properties.corppass!!.homepageUrl)
            .queryParam("NameIdFormat", "Email")
            .queryParam("esrvcID", idp.serviceId)
            .build()
            .encode()
        return "redirect:${uriComponents.toUriString()}"
    }

    @GetMapping("/cb")
    fun callback(model: Model, @RequestParam(value="SAMLart") artifactId: String, @RequestParam(value="RelayState") relayState: String, request: HttpServletRequest): String {
        try {
            val resolved = ArtifactResolver.resolveArtifact(artifactId, request, properties.corppass!!, ServiceProvider.Corppass, IdentityProvider.Corppass)
            val user = DocumentBuilder.parse<User>(resolved.entries.single().value as String, "User")

            val token: String = jwt.buildCorppass(user.toMap())

            model.addAttribute("model", object {
                val postUrl: String = properties.corppass!!.serviceProvider!!.loginUrl
                val token: String = token
                val relayState: String = relayState
            })

            return "relay"
        } catch (e: Exception) {
            e.printStackTrace()
            return "error"
        }
    }
}