package dev.medzik.librepass.server.components

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
    ): UserTable? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: return null
        val authorizationHeader = request.getHeader("Authorization")
            ?: return null
        val token = authorizationHeader.removePrefix("Bearer ")

        // parse token
        val tokenClaims = authComponent.parseToken(TokenType.ACCESS_TOKEN, token)
            ?: return null
        // get user id from token
        val userID = tokenClaims["sub"] as String

        // get user from database
        val user = userRepository
            .findById(UUID.fromString(userID))
            .orElse(null)
            ?: return null

        // check that the token was created after the last password change to prevent cipher corruption
        if (tokenClaims.expiration > user.lastPasswordChange) {
            return null
        }

        // return user table from database
        return user
    }
}
