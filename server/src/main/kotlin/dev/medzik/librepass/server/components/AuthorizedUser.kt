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
 * If a user is not authorized, then null will be returned.
 * If a user is authorized, then [UserTable] will be returned.
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
    ): UserTable? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: return null

        val authorizationHeader = request.getHeader("Authorization")
            ?: return null
        val token = authorizationHeader.removePrefix("Bearer ")

        // parse token
        val tokenClaims = authComponent.parseToken(TokenType.API_KEY, token)
            ?: return null
        // get user id from token
        val userID = tokenClaims[TokenClaims.USER_ID.key] as String

        // get user from database
        val user = userRepository
            .findById(UUID.fromString(userID))
            .orElse(null)
            ?: return null

        // check if user changed password after the token was issued
        if (user.lastPasswordChange > tokenClaims.issuedAt) {
            return null
        }

        // return user table from the database
        return user
    }
}
