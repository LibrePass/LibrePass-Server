package dev.medzik.librepass.server.components

import dev.medzik.librepass.server.controllers.advice.AuthorizedUserException
import dev.medzik.librepass.server.database.TokenRepository
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.utils.checkIfElapsed
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.MethodParameter
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
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
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    @Value("\${http.ip.header}") private val ipHeader: String
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

        val tokenTable = tokenRepository.findByIdOrNull(token)
            ?: throw AuthorizedUserException()

        val user = userRepository
            .findById(tokenTable.owner)
            .orElse(null)
            ?: throw AuthorizedUserException()

        // TODO: Expire inactive tokens after some time.

        // check if user changed password after the token was created
        if (user.lastPasswordChange > tokenTable.created)
            throw AuthorizedUserException()

        val ip = if (ipHeader == "remoteAddr")
            request.remoteAddr
        else request.getHeader(ipHeader)
            ?: request.remoteAddr

        // check if the IP address has been changed
        // or if 5 minutes elapsed since the date of last use
        val currentDate = Date()
        if (tokenTable.lastIp != ip ||
            checkIfElapsed(tokenTable.lastUsed, currentDate, 5)
        ) {
            tokenRepository.save(
                tokenTable.copy(
                    lastIp = ip,
                    lastUsed = currentDate
                )
            )
        }

        return user
    }

    /**
     * Delete expired tokens every 30 minutes.
     */
    @Scheduled(cron = "0 */30 * ? * *")
    fun deleteExpiredTokens() {
        val currentDate = Date()
        val date30DaysAgo = Date(currentDate.time - 1000L * 60 * 60 * 24 * 30)

        tokenRepository.findAllByLastUsedBefore(date30DaysAgo).forEach { token ->
            tokenRepository.delete(token)
        }
    }
}
