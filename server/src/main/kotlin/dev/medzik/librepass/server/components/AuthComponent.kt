package dev.medzik.librepass.server.components

import dev.medzik.librepass.server.utils.KeyParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*

/**
 * One hour in milliseconds. Used for token expiration time.
 */
private const val HourTime = 1000L * 60 * 60

/**
 * Type of the token.
 * @param expirationTime Time in milliseconds for which the token is valid.
 */
enum class TokenType(val expirationTime: Long) {
    ACCESS_TOKEN(10 * HourTime), // 10 hours
    REFRESH_TOKEN(90 * 24 * HourTime), // 90 days
    VERIFICATION_TOKEN(24 * HourTime) // 24 hours
}

@Component
class AuthComponent
@Autowired constructor(
    @Value("\${jwt.publicKeyFile}") publicKeyFile: String,
    @Value("\${jwt.privateKeyFile}") privateKeyFile: String
) {
    private lateinit var publicKey: PublicKey
    private lateinit var privateKey: PrivateKey

    init {
        this.publicKey = KeyParser.parsePublicKey(publicKeyFile)
        this.privateKey = KeyParser.parsePrivateKey(privateKeyFile)
    }

    /**
     * Generates a token for the given user.
     * @param tokenType Type of the token.
     * @param userId User ID.
     * @return Generated token.
     */
    fun generateToken(tokenType: TokenType, userId: UUID): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["typ"] = tokenType.name
        claims["sub"] = userId

        return Jwts.builder()
            .setClaims(claims)
            .setExpiration(Date(System.currentTimeMillis() + tokenType.expirationTime))
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact()
    }

    /**
     * Parses a token and returns the user id. Returns null if the token is invalid.
     * @param tokenType Type of the token.
     * @param token Token to be parsed.
     * @return User ID or null.
     */
    fun parseToken(tokenType: TokenType, token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .body

            // check if the token type is correct
            if (claims["typ"] != tokenType.name) return null

            claims["sub"] as String
        } catch (e: Exception) {
            null
        }
    }
}
