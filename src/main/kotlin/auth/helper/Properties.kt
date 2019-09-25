package auth.helper

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import auth.plugin.ClaimsMutator
import auth.plugin.DefaultClaimsMutator

@Configuration
@ConfigurationProperties(prefix = "app")
class Properties : ApplicationContextAware {
    var token: Token? = null
    var singpass: Provider? = null
    var corppass: Provider? = null
    var service: Services? = null
    var instrumentation: Instrumentation? = null

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

class Instrumentation {
    var zipkin: ZipkinInstrumentation? = null
}

class ZipkinInstrumentation {
    lateinit var url: String
    var env: String? = null
}

class Token {
    lateinit var privateKey: String
    lateinit var publicKeyPath: String
    lateinit var signatureAlgorithm: String
    var encryptionJwk: String? = null
    var expirationTime: Long = 0
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
    var serviceProvider: ServiceProvider? = null
    var identityProvider: IdentityProvider? = null
    var additionalInfoRequest = AdditionalInfoRequest()
}

class ServiceProvider {
    lateinit var loginUrl: String
    lateinit var privateKey: String
    lateinit var metadataPath: String
    lateinit var metadataId: String
}

class IdentityProvider {
    lateinit var host: String
    lateinit var serviceId: String
    lateinit var metadataPath: String
    lateinit var metadataId: String
    var artifactServiceProxyHost: String? = null
    var artifactServiceProxyPort: Int? = null
    var artifactServiceProxyUsername: String? = null
    var artifactServiceProxyPassword: String? = null
    var artifactLifetimeClockSkew: Long = 0
}

class AdditionalInfoRequest {
    var url: String? = null
    var httpMethod: String = "GET"
    var body: String? = null
    var staticJson: String? = null
    val static: Map<String, Any>? by lazy {
        var staticMap: Map<String, Any>?
        if (this.staticJson == null) {
            staticMap = null
        } else {
            val mapper = ObjectMapper()
            staticMap = mapper.readValue(this.staticJson, object : TypeReference<Map<String, Any>>() {})
        }
        staticMap
    }
}
