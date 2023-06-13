package dev.medzik.librepass.server

import dev.medzik.librepass.server.components.AuthorizedUserArgumentResolver
import dev.medzik.librepass.server.components.RequestIPArgumentResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig @Autowired constructor(
    // For @AuthorizedUser annotation
    private val authorizedUserArgumentResolver: AuthorizedUserArgumentResolver,
    // For @RequestIP annotation
    private val requestIPArgumentResolver: RequestIPArgumentResolver
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        // Add @AuthorizedUser annotation support
        resolvers.add(authorizedUserArgumentResolver)
        // Add @RequestIP annotation support
        resolvers.add(requestIPArgumentResolver)
    }

    // CORS configuration
    @Value("\${server.cors.allowedOrigins}")
    private lateinit var allowedOrigins: String
    override fun addCorsMappings(registry: CorsRegistry) {
        val allowedOrigin = allowedOrigins.split(",").toTypedArray()

        registry.addMapping("/**")
            .allowedOrigins(*allowedOrigin)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
