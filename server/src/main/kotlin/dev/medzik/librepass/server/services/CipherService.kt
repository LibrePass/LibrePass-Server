package dev.medzik.librepass.server.services

import dev.medzik.librepass.server.database.CipherRepository
import dev.medzik.librepass.server.database.CipherTable
import dev.medzik.librepass.types.api.EncryptedCipher
import dev.medzik.librepass.types.api.cipher.SyncResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*

@Service
class CipherService {
    @Autowired
    private lateinit var cipherRepository: CipherRepository

    @Throws(IOException::class)
    fun updateCipher(encryptedCipher: EncryptedCipher) {
        val cipher = CipherTable(encryptedCipher)

        val owner = cipherRepository.getOwnerOfCipher(encryptedCipher.id)
        if (owner != encryptedCipher.owner) {
            // TODO: may be custom error class?
            throw IOException("Cipher owner mismatch")
        }

        cipherRepository.save(cipher)
    }

    fun insertCipher(encryptedCipher: EncryptedCipher): CipherTable =
        cipherRepository.save(CipherTable(encryptedCipher))

    fun checkIfCipherExistsAndOwnedBy(id: UUID, owner: UUID): Boolean =
        cipherRepository.checkIfCipherExistsAndOwnedBy(id, owner)

    fun getCipher(id: UUID, owner: UUID): CipherTable? {
        val cipher = cipherRepository.findById(id).orElse(null) ?: return null
        if (cipher.owner != owner) return null
        return cipher
    }

    fun getAllCiphers(owner: UUID): List<CipherTable> =
        cipherRepository.getAll(owner)

    fun sync(owner: UUID, timestamp: Date): SyncResponse {
        val ciphers = getAllCiphers(owner)

        return SyncResponse(
            // get ids of all ciphers
            ids = ciphers.map { it.id },
            // get all ciphers that were updated after timestamp
            ciphers = ciphers
                // get all ciphers that were updated after timestamp
                .filter { it.lastModified.after(timestamp) }
                // convert to encrypted ciphers
                .map { it.toEncryptedCipher() }
        )
    }

    fun deleteCipher(id: UUID) =
        cipherRepository.deleteById(id)
}
