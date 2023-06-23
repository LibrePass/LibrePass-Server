package dev.medzik.librepass.server.components

import dev.medzik.librepass.server.controllers.advice.AuthorizedUserException
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.*

/**
 * Annotation for getting authorized user from request.
 * @see AuthorizedUserArgumentResolver
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
annotation class AuthorizedUser

@Component
class AuthorizedUserArgumentResolver @Autowired constructor(
    private val authComponent: AuthComponent,
    private val userRepository: UserRepository
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthorizedUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UserTable {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw Exception("Failed to get native HttpServletRequest")

        val authorizationHeader = request.getHeader("Authorization")
            ?: throw AuthorizedUserException()
        val token = authorizationHeader.removePrefix("Bearer ")

        val tokenClaims = authComponent.parseToken(TokenType.API_KEY, token)
            ?: throw AuthorizedUserException()
        val userID = tokenClaims[TokenClaims.USER_ID.key] as String

        // get user from database
        val user = userRepository
            .findById(UUID.fromString(userID))
            .orElse(null)
            ?: throw AuthorizedUserException()

        // check if user changed password after the token was issued
        if (user.lastPasswordChange > tokenClaims.issuedAt)
            throw AuthorizedUserException()

        return user
    }
}
