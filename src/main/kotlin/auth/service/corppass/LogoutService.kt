package auth.service.corppass

import auth.util.helper.Properties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder

public interface LogoutService {
    fun getLogoutDestination(returnUrl: String): String
}

@Service("CorppassLogoutService")
@Profile("qa", "development", "remote-development")
public class DevelopmentLogoutService : LogoutService {
    override fun getLogoutDestination(returnUrl: String): String {
        val uriComponents: UriComponents = UriComponentsBuilder
            .fromHttpUrl(returnUrl)
            .build()
            .encode()
        return uriComponents.toUriString()
    }
}

@Service("CorppassLogoutService")
@Profile("uat", "production")
public class ProductionLogoutService : LogoutService {
    @Autowired
    lateinit var properties: Properties

    override fun getLogoutDestination(returnUrl: String): String {
        val oidp = properties.corppass.openIdProvider
        var uriComponents: UriComponents
        if (oidp.logoutEndpoint != null) {
            uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(oidp.host)
                .path(oidp.logoutEndpoint)
                .queryParam("return_url", "{returnUrl}")
                .encode()
                .buildAndExpand(returnUrl)
        } else {
            uriComponents = UriComponentsBuilder
                .fromHttpUrl(returnUrl)
                .build()
                .encode()
        }
        return uriComponents.toUriString()
    }
}
