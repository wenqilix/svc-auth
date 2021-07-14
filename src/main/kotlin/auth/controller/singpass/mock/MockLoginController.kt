package auth.controller.mock.singpass

import auth.model.singpass.User
import com.fasterxml.jackson.databind.ObjectMapper
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

@Controller("SingpassMockLoginController")
@RequestMapping("/sp/mock")
@Profile("qa", "development", "remote-development")
class MockLoginController {
    @GetMapping("/login")
    fun login(model: Model, @RequestParam(value = "state") state: String): String {
        model.addAttribute("user", User())
        model.addAttribute("state", state)

        return "singpass"
    }

    @PostMapping("/submit")
    fun callback(user: User, @RequestParam(value = "state") state: String): String {
        val mapper = ObjectMapper()
        val authCode = String(Base64.getEncoder().encode(mapper.writeValueAsBytes(user)))

        val uriComponents: UriComponents = UriComponentsBuilder.newInstance()
            .path("/sp/cb")
            .queryParam("code", authCode)
            .queryParam("state", state)
            .build()
            .encode()
        return "redirect:${uriComponents.toUriString()}"
    }
}
