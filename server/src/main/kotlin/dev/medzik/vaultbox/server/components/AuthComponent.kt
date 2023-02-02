package dev.medzik.vaultbox.server.components

import dev.medzik.vaultbox.server.utils.KeyParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*

enum class TokenType(val type: String) {
    ACCESS_TOKEN("access_token"),
    REFRESH_TOKEN("refresh_token"),
    VERIFICATION_TOKEN("verification_token")
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
        this.privateKey = KeyParser.parsePrivateKey(privateKeyFile);
    }

    /**
     * Generates a token for the given user.
     * @param type Type of the token.
     * @param userId User ID.
     * @return Generated token.
     */
    fun generateToken(type: TokenType, userId: UUID): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["typ"] = type.type
        claims["sub"] = userId

        return Jwts.builder()
            .setClaims(claims)
            .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // TODO: get from config
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact()
    }

    /**
     * Parses a token and returns the user id. Returns null if the token is invalid.
     * @param token Token to be parsed.
     * @return User ID or null.
     */
    fun parseToken(type: TokenType, token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .body

            if (claims["typ"] != type.type) return null

            claims["sub"] as String
        } catch (e: Exception) {
            null
        }
    }
}
