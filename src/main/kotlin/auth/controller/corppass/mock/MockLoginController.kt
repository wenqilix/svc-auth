package auth.controller.mock.corppass

import auth.model.corppass.AuthAccess
import auth.model.corppass.ThirdPartyAuthAccess
import auth.model.corppass.User
import auth.model.corppass.UserInfo
import auth.model.corppass.auth.Auth
import auth.model.corppass.auth.ThirdPartyClient
import auth.model.corppass.mock.UserList
import auth.util.helper.Properties
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.util.Base64

@Suppress("StringLiteralDuplication")
@Controller("CorppassMockLoginController")
@RequestMapping("/cp/mock")
@Profile("qa", "development", "remote-development")
class MockLoginController {
    @Autowired
    lateinit var properties: Properties

    @GetMapping("/login")
    fun login(model: Model, @RequestParam(value = "state") state: String): String {
        val mockUserList = UserList()
        if (properties.corppass.mockUserListUrl != null) {
            mockUserList.populateFrom(properties.corppass.mockUserListUrl!!)
        }
        model.addAttribute("userList", mockUserList)
        model.addAttribute("userInfo", UserInfo())
        model.addAttribute("authAccess", AuthAccess())
        model.addAttribute("thirdPartyAuthAccess", ThirdPartyAuthAccess())
        model.addAttribute("auth", Auth())
        model.addAttribute("state", state)

        return "corppass"
    }

    @PostMapping("/submit")
    fun callback(
        userInfo: UserInfo,
        @RequestParam(value = "state") state: String,
        @RequestParam(value = "authAccessState") authAccessState: String,
        @RequestParam(value = "thirdPartyAuthAccessState") thirdPartyAuthAccessState: String
    ): String {
        val mapper = ObjectMapper()
        val authAccess = mapper.readValue(authAccessState, AuthAccess::class.java)
        val thirdPartyAuthAccess = mapper.readValue(thirdPartyAuthAccessState, ThirdPartyAuthAccess::class.java)

        if (userInfo.entityType == "UEN") {
            userInfo.entityRegNo = null
            userInfo.entityCountry = null
            userInfo.entityName = null
        } else {
            userInfo.entityStatus = null
        }

        val user = User(userInfo, authAccess, thirdPartyAuthAccess)
        val authCode = String(Base64.getEncoder().encode(mapper.writeValueAsBytes(user)))

        val uriComponents: UriComponents = UriComponentsBuilder.newInstance()
            .path("/cp/cb")
            .queryParam("code", authCode)
            .queryParam("state", state)
            .build()
            .encode()
        return "redirect:${uriComponents.toUriString()}"
    }

    private fun populateModelState(model: Model, params: Map<String, String>) {
        val mapper = ObjectMapper()
        if (!params.get("userInfoState").isNullOrEmpty()) {
            model.addAttribute("userInfo", mapper.readValue(params.get("userInfoState"), UserInfo::class.java))
        }
        model.addAttribute("userList", mapper.readValue(params.get("userListState"), UserList::class.java))
        model.addAttribute("authAccess", mapper.readValue(params.get("authAccessState"), AuthAccess::class.java))
        model.addAttribute(
            "thirdPartyAuthAccess",
            mapper.readValue(params.get("thirdPartyAuthAccessState"), ThirdPartyAuthAccess::class.java)
        )
        model.addAttribute("state", params.get("state"))
    }

    @PostMapping("/role", params = ["add"])
    fun addRole(
        auth: Auth,
        model: Model,
        @RequestParam(value = "thirdParty", required = false) thirdParty: Boolean?,
        @RequestParam requestParams: Map<String, String>
    ): String {
        populateModelState(model, requestParams)

        val thirdPartyAuthAccess = model.asMap().get("thirdPartyAuthAccess") as ThirdPartyAuthAccess
        val authAccess = model.asMap().get("authAccess") as AuthAccess
        val clientEntityId = requestParams.getOrDefault("clientEntityId", "")
        val clientEntityType = requestParams.getOrDefault("clientEntityType", "")

        if (auth.endDate.isNullOrEmpty()) {
            auth.endDate = "9999-12-31"
        }

        if (thirdParty != null && thirdParty) {
            val clientIndex = thirdPartyAuthAccess.clients.indexOfFirst {
                client ->
                client.entityId == clientEntityId && client.entityType == clientEntityType
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

        model.addAttribute("authAccess", authAccess)
        model.addAttribute("thirdPartyAuthAccess", thirdPartyAuthAccess)
        model.addAttribute("auth", Auth())

        return "corppass"
    }

    @PostMapping("/role", params = ["remove"])
    fun removeRole(
        model: Model,
        @RequestParam(value = "remove") index: Int,
        @RequestParam requestParams: Map<String, String>
    ): String {
        populateModelState(model, requestParams)

        val authAccess = model.asMap().get("authAccess") as AuthAccess

        val auths = authAccess.auths
        auths.removeAt(index)

        model.addAttribute("authAccess", authAccess)
        model.addAttribute("auth", Auth())

        return "corppass"
    }

    @PostMapping("/role", params = ["removeThirdParty"])
    fun removeThirdPartyRole(
        model: Model,
        @RequestParam(value = "removeThirdParty") indexes: String,
        @RequestParam requestParams: Map<String, String>
    ): String {
        val index = indexes.split(',')
        populateModelState(model, requestParams)

        val thirdPartyAuthAccess = model.asMap().get("thirdPartyAuthAccess") as ThirdPartyAuthAccess

        val clientIndex = index.get(0).toInt()
        val auths = thirdPartyAuthAccess.clients.get(clientIndex).auths
        auths.removeAt(index.get(1).toInt())

        model.addAttribute("thirdPartyAuthAccess", thirdPartyAuthAccess)
        model.addAttribute("auth", Auth())

        return "corppass"
    }

    @PostMapping("/userInfo", params = ["select"])
    fun selectUserInfo(
        model: Model,
        @RequestParam(value = "select") index: Int,
        @RequestParam requestParams: Map<String, String>
    ): String {
        populateModelState(model, requestParams)

        val userList = model.asMap().get("userList") as UserList
        val user = userList.users.get(index)

        model.addAttribute("userInfo", user.userInfo)
        model.addAttribute("authAccess", user.authAccess)
        model.addAttribute("thirdPartyAuthAccess", user.thirdPartyAuthAccess)
        model.addAttribute("auth", Auth())

        return "corppass"
    }
}
