package dev.medzik.librepass.server

import dev.medzik.librepass.server.components.AuthorizedUserArgumentResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    @Value("\${cors.allowedOrigins}")
    private lateinit var allowedOrigins: String

    @Autowired
    private lateinit var authorizedUserArgumentResolver: AuthorizedUserArgumentResolver

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authorizedUserArgumentResolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        val allowedOrigin = allowedOrigins.split(",").toTypedArray()

        registry.addMapping("/**")
            .allowedOrigins(*allowedOrigin)
    }
}
