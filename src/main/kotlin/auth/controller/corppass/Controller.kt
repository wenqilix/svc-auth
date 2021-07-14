package auth.controller.corppass

import auth.service.corppass.LoginService
import auth.service.corppass.LogoutService
import auth.util.helper.Properties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.Base64

@Controller("CorppassController")
@RequestMapping("/cp")
class Controller {
    @Autowired
    lateinit var loginService: LoginService
    @Autowired
    lateinit var logoutService: LogoutService
    @Autowired
    lateinit var properties: Properties

    @GetMapping("/login")
    fun login(@RequestParam(value = "target", required = false) target: String?): String {
        val identityProviderLoginDestination = loginService.getIdentityProviderLoginDestination(target)
        return "redirect:$identityProviderLoginDestination"
    }

    @GetMapping("/cb")
    fun callback(
        @RequestParam(value = "code", required = false) code: String,
        @RequestParam(value = "state", required = false) state: String
    ): String {
        val stateContentByteArray = Base64.getDecoder().decode(state.toByteArray())
        val mapper = ObjectMapper()
        val stateContent = mapper.readValue(stateContentByteArray, object : TypeReference<Map<String, String>>() {})

        val identityConsumerCallbackDestination =
            loginService.getIdentityConsumerCallbackDestination(code, stateContent.getValue("target"))
        return "redirect:$identityConsumerCallbackDestination"
    }

    @GetMapping("/logout")
    fun logout(@RequestParam(value = "returnUrl") returnUrl: String): String {
        val logoutDestination = logoutService.getLogoutDestination(returnUrl)
        return "redirect:$logoutDestination"
    }
}
