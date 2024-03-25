package dev.medzik.librepass.server.components.schedulers

import dev.medzik.librepass.server.database.TokenRepository
import dev.medzik.librepass.server.database.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class DeleteDatabaseEntries @Autowired constructor(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    @Value("\${email.verification.required}")
    private val emailVerificationRequired: Boolean
) {
    /**
     * Delete unverified users who have not verified their account for more than 24 hours.
     *
     * This task is runs every 12 hours.
     */
    @Scheduled(cron = "0 */12 * * * *")
    fun deleteUnverifiedAccountsTask() {
        if (emailVerificationRequired) {
            val yesterdayDate = Instant.now().minusSeconds(TimeUnit.DAYS.toSeconds(1))
            userRepository.deleteUnverifiedUsers(Date.from(yesterdayDate))
        }
    }

    /**
     * Delete unused tokens that were not used for more than 30 days.
     *
     * This task is run every 12 hours.
     */
    @Scheduled(cron = "0 */12 * * * *")
    fun deleteUnusedTokensTask() {
        val dateThirtyDaysAgo = Instant.now().minusSeconds(TimeUnit.DAYS.toSeconds(30))
        tokenRepository.deleteUnused(Date.from(dateThirtyDaysAgo))
    }
}
