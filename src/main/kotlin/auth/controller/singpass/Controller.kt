package auth.controller.singpass

import auth.service.singpass.LoginService
import auth.util.helper.Properties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.Base64

@Controller("SingpassController")
@RequestMapping("/sp")
class Controller {
    @Autowired
    lateinit var loginService: LoginService
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
}
