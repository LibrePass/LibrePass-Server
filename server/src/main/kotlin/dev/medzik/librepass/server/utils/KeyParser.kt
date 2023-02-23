package dev.medzik.librepass.server.utils

import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

internal object KeyParser {
    @Throws(Exception::class)
    fun parsePrivateKey(file: String): PrivateKey {
        // read private key from file
        val keyFile = FileInputStream(file)
        var keyBytes = keyFile.readAllBytes()
        keyFile.close()

        // convert PEM-formatted key to PKCS#8
        var pem = String(keyBytes, StandardCharsets.UTF_8)
        pem = pem.replace("-----BEGIN PRIVATE KEY-----\n", "")
        pem = pem.replace("-----END PRIVATE KEY-----", "")
        pem = pem.replace("\\s".toRegex(), "")
        keyBytes = Base64.getDecoder().decode(pem)

        // create PrivateKey object
        val spec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(spec)
    }

    @Throws(Exception::class)
    fun parsePublicKey(file: String): PublicKey {
        // read public key from file
        val keyFile = FileInputStream(file)
        var keyBytes = keyFile.readAllBytes()
        keyFile.close()

        // convert PEM-formatted key to PKCS#8
        var pem = String(keyBytes, StandardCharsets.UTF_8)
        pem = pem.replace("-----BEGIN PUBLIC KEY-----\n", "")
        pem = pem.replace("-----END PUBLIC KEY-----", "")
        pem = pem.replace("\\s".toRegex(), "")
        keyBytes = Base64.getDecoder().decode(pem)

        // create PublicKey object
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec)
    }
}
