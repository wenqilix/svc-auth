package auth.util.helper

import auth.plugin.ClaimsMutator
import auth.plugin.DefaultClaimsMutator
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
class Properties : ApplicationContextAware {
    var token: Token = Token()
    var singpass: Provider = Provider()
    var corppass: Provider = Provider()
    var service: Services = Services()

    companion object {
        private lateinit var applicationContext: ApplicationContext

        fun getPropertiesContext(): Properties {
            return applicationContext.getBeansOfType(Properties::class.java).values.iterator().next()
        }
    }

    override fun setApplicationContext(context: ApplicationContext) {
        applicationContext = context
    }
}

class Services {
    lateinit var servicesFolderPath: String
    var signatureLifetimeClockSkew: Long = 0
}

class Token {
    lateinit var signingJwk: String
    var encryptionJwksUrl: String? = null
    var expirationTime: Long = 0
    var maxAge: Long = 12 * 60 * 60 * 1000
    var plugin = Plugin()
}

class Plugin {
    var jarFileUrl: String? = null
    var classPath: String? = null
    val instance: ClaimsMutator by lazy {
        if (this.jarFileUrl != null && this.classPath != null) {
            val loader = PluginLoader<ClaimsMutator>()
            val plugin = loader.loadClass(this.jarFileUrl!!, this.classPath!!)
            plugin
        } else {
            DefaultClaimsMutator()
        }
    }
}

class Provider {
    lateinit var homepageUrl: String
    var mockUserListUrl: String? = null
    var serviceProvider: ServiceProvider = ServiceProvider()
    var openIdProvider: OpenIdProvider = OpenIdProvider()
    var additionalInfoRequest = AdditionalInfoRequest()
}

class ServiceProvider {
    lateinit var loginUrl: String
}

class OpenIdProvider {
    lateinit var host: String
    lateinit var clientId: String
    lateinit var redirectUri: String
    var token: OpenIdProviderToken = OpenIdProviderToken()
    var authorizeEndpoint: String = "/authorize"
    var tokenEndpoint: String = "/token"
    var authorizationInfoEndpoint: String? = null
    var logoutEndpoint: String? = null
    var clientAssertionJwtExpirationTime: Long = 2 * 60 * 1000
}

class OpenIdProviderToken {
    var encryptionJwk: String? = null
    var signingJwk: String? = null
    var jwksEndpoint: String? = null
}
