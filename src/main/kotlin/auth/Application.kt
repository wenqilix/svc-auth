package auth

import brave.Tracing
import brave.opentracing.BraveTracer
import brave.sampler.Sampler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import zipkin.Span
import zipkin.reporter.okhttp3.OkHttpSender
import zipkin.reporter.AsyncReporter
import zipkin.reporter.Encoding

import auth.helper.Properties

const val APPLICATION_NAME = "svc-auth"
const val ZIPKIN_HOSTNAME_PROPERTIES_PATH = "app.instrumentation.zipkin"
const val ZIPKIN_HOSTNAME_PROPERTIES_KEY = "url"
const val ZIPKIN_TRACING_URL_PATH = "/api/v1/spans"

@SpringBootApplication
class Application {
    @Autowired
    var properties: Properties = Properties()

    private fun getZipkinApiEndpoint(): String {
        val zipkinServerUrl: String =
            (properties.instrumentation?.zipkin?.url).toString()
        return "${zipkinServerUrl}${ZIPKIN_TRACING_URL_PATH}"
    }

    private fun getZipkinApplicationName(applicationName: String): String {
        val zipkinEnvironment: String =
            (properties.instrumentation?.zipkin?.env).toString()
        return if (zipkinEnvironment == "null") applicationName
            else "${applicationName}-${zipkinEnvironment}"
    }

    @Bean
    @ConditionalOnProperty(
        prefix=ZIPKIN_HOSTNAME_PROPERTIES_PATH,
        value=[ZIPKIN_HOSTNAME_PROPERTIES_KEY],
        matchIfMissing=false
    )
    fun zipkinTracer(): io.opentracing.Tracer {
        val okHttpSender: OkHttpSender =
            OkHttpSender
                .create(getZipkinApiEndpoint())
        val reporter: AsyncReporter<Span> =
            AsyncReporter
                .builder(okHttpSender)
                .build()
        val braveTracer: Tracing =
            Tracing
                .newBuilder()
                .localServiceName(
                    getZipkinApplicationName(APPLICATION_NAME)
                )
                .reporter(reporter)
                .build()
        return BraveTracer.create(braveTracer)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
