package dev.medzik.librepass.server.components

import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.services.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Annotation for getting authorized user from request.
 * If user is not authorized, then null will be passed.
 * If user is authorized, then [UserTable] will be passed.
 * @see AuthorizedUserArgumentResolver
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
annotation class AuthorizedUser

/**
 * Argument resolver for [AuthorizedUser] annotation.
 * @see AuthorizedUser
 */
@Component
class AuthorizedUserArgumentResolver : HandlerMethodArgumentResolver {
    @Autowired
    private lateinit var userService: UserService

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthorizedUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UserTable? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java) ?: return null
        val authorizationHeader = request.getHeader("Authorization") ?: return null
        val token = authorizationHeader.removePrefix("Bearer ")
        return userService.getUserByToken(token)
    }
}
