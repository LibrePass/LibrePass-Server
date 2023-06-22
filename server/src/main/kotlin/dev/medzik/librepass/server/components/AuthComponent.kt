package dev.medzik.librepass.server.components

import dev.medzik.libcrypto.RSA
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*

/**
 * TokenType represents token types with expiration time.
 */
enum class TokenType(val expirationTime: Long) {
    API_KEY(90 * 24 * 60 * 60 * 1000L), // 90 days
}

/**
 * TokenClaims represents claims in the jsonwebtoken.
 */
enum class TokenClaims(val key: String) {
    TYPE("typ"),
    USER_ID("sub"),
}

@Component
class AuthComponent @Autowired constructor(
    @Value("\${jwt.publicKeyFile}") publicKeyFile: String,
    @Value("\${jwt.privateKeyFile}") privateKeyFile: String
) {
    private lateinit var publicKey: PublicKey
    private lateinit var privateKey: PrivateKey

    init {
        // read public key from file
        val publicKeyFileStream = FileInputStream(publicKeyFile)
        val publicKeyString = publicKeyFileStream.readAllBytes().toString(Charsets.UTF_8)
        publicKeyFileStream.close()

        // read private key from file
        val privateKeyFileStream = FileInputStream(privateKeyFile)
        val privateKeyString = privateKeyFileStream.readAllBytes().toString(Charsets.UTF_8)
        privateKeyFileStream.close()

        // parse keys from strings
        this.publicKey = RSA.KeyUtils.getPublicKey(publicKeyString)
        this.privateKey = RSA.KeyUtils.getPrivateKey(privateKeyString)
    }

    /**
     * Generates a token for the given user.
     * @param tokenType token type
     * @param userId user identifier
     * @return Generated token as a string.
     */
    fun generateToken(tokenType: TokenType, userId: UUID): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims[TokenClaims.TYPE.key] = tokenType.name
        claims[TokenClaims.USER_ID.key] = userId

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + tokenType.expirationTime))
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact()
    }

    /**
     * Parses a token and returns the user id. Returns null if the token is invalid.
     * @param type token type
     * @param token token to be parsed
     * @return Token claims or null if the token is invalid.
     */
    fun parseToken(type: TokenType, token: String): Claims? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .body

            // check if the token type is correct
            if (claims[TokenClaims.TYPE.key] != type.name)
                return null

            claims
        } catch (e: Exception) {
            null
        }
    }
}
