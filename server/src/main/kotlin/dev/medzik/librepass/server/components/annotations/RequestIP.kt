package dev.medzik.librepass.server.components.annotations

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/** Get client IP from request. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
annotation class RequestIP

@Component
class RequestIPArgumentResolver
    @Autowired
    constructor(
        @Value("\${http.ip.header}")
        private val ipHeader: String
    ) : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.hasParameterAnnotation(RequestIP::class.java)
        }

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): String? {
            val request =
                webRequest.getNativeRequest(HttpServletRequest::class.java)
                    ?: return null

            return when (ipHeader) {
                "remoteAddr" -> request.remoteAddr
                else -> request.getHeader(ipHeader)
            }
        }
    }
