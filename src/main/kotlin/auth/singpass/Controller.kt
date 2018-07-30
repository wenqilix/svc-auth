package auth.singpass

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import auth.helper.Properties
import auth.helper.Jwt
import auth.saml.artifact.ArtifactResolver
import auth.saml.credentials.ServiceProvider
import auth.saml.credentials.IdentityProvider
import auth.singpass.model.User

@Controller
@RequestMapping("/sp")
@Profile("qa", "development", "remote-development")
class DevelopmentController {
    @Autowired
    lateinit var properties: Properties
    @Autowired
    lateinit var jwt: Jwt

    @GetMapping("/login")
    fun login(model: Model, @RequestParam(value="target", required=false) target: String?): String {
        model.addAttribute("user", User())
        model.addAttribute("target", target ?: properties.singpass!!.homepageUrl)

        return "singpass"
    }
 
    @GetMapping("/cb")
    fun callback(user: User, model: Model, @RequestParam(value="RelayState") relayState: String): String {
        val token: String = jwt.buildSingpass(user.toMap())

        model.addAttribute("model", object {
            val postUrl: String = properties.singpass!!.serviceProvider!!.loginUrl
            val token: String = token
            val relayState: String = relayState
        })

        return "relay"
    }
}

@Controller
@RequestMapping("/sp")
@Profile("uat", "production")
class ProductionController {
    @Autowired
    lateinit var properties: Properties
    @Autowired
    lateinit var jwt: Jwt

    @GetMapping("/login")
    fun login(@RequestParam(value="target", required=false) target: String?): String {
        val idp = properties.singpass!!.identityProvider!!
        val uriComponents: UriComponents = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(idp.host)
            .path("/FIM/sps/SingpassIDPFed/saml20/logininitial")
            .queryParam("RequestBinding", "HTTPArtifact")
            .queryParam("ResponseBinding", "HTTPArtifact")
            .queryParam("PartnerId", ServiceProvider.Singpass.ENTITY_ID)
            .queryParam("Target", target ?: properties.singpass!!.homepageUrl)
            .queryParam("NameIdFormat", "Email")
            .queryParam("esrvcID", idp.serviceId)
            .build()
            .encode()
        return "redirect:${uriComponents.toUriString()}"
    }

    @GetMapping("/cb")
    fun callback(model: Model, @RequestParam(value="SAMLart") artifactId: String, @RequestParam(value="RelayState") relayState: String, request: HttpServletRequest): String {
        try {
            val resolved = ArtifactResolver.resolveArtifact(artifactId, request, properties.singpass!!, ServiceProvider.Singpass, IdentityProvider.Singpass)
            val user = User(resolved)

            val token: String = jwt.buildSingpass(user.toMap())

            model.addAttribute("model", object {
                val postUrl: String = properties.singpass!!.serviceProvider!!.loginUrl
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
