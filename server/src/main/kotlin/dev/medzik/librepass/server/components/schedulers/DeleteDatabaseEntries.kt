package dev.medzik.librepass.server.components.schedulers

import dev.medzik.librepass.server.database.TokenRepository
import dev.medzik.librepass.server.database.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class DeleteDatabaseEntries
    @Autowired
    constructor(
        private val userRepository: UserRepository,
        private val tokenRepository: TokenRepository,
        @Value("\${email.verification.required}")
        private val emailVerificationRequired: Boolean,
    ) {
        /**
         * Delete unverified users who have not verified their account for more than 24 hours.
         *
         * This task is run once every 12 hours.
         */
        @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
        fun deleteUnverifiedAccountsTask() {
            if (emailVerificationRequired) {
                val yesterdayDate = Instant.now().minusSeconds(24 * 60 * 60)
                userRepository.deleteUnverifiedUsers(Date.from(yesterdayDate))
            }
        }

        /**
         * Delete unused tokens that were not used for more than 30 days.
         *
         * This task is run once every 12 hours.
         */
        @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
        fun deleteUnusedTokensTask() {
            val dateThirtyDaysAgo = Instant.now().minusSeconds(30 * 24 * 60 * 60)
            tokenRepository.deleteUnused(Date.from(dateThirtyDaysAgo))
        }
    }
