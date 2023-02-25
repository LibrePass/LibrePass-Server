package dev.medzik.librepass.server.services

import dev.medzik.librepass.server.database.CipherRepository
import dev.medzik.librepass.server.database.CipherTable
import dev.medzik.librepass.types.api.EncryptedCipher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class CipherService {
    @Autowired
    private lateinit var cipherRepository: CipherRepository

    @Throws(Exception::class)
    fun updateCipher(encryptedCipher: EncryptedCipher) {
        val cipher = CipherTable()
        cipher.from(encryptedCipher)

        val owner = cipherRepository.getOwnerOfCipher(encryptedCipher.id)
        if (owner != encryptedCipher.owner) {
            // TODO: may be custom error class?
            throw Exception("Cipher owner mismatch")
        }

        cipherRepository.save(cipher)
    }

    fun insertCipher(encryptedCipher: EncryptedCipher): CipherTable {
        val cipher = CipherTable()
        cipher.from(encryptedCipher)
        return cipherRepository.save(cipher)
    }

    fun checkIfCipherExistsAndOwnedBy(id: UUID, owner: UUID): Boolean = cipherRepository.checkIfCipherExistsAndOwnedBy(id, owner)

    fun getCipher(id: UUID, owner: UUID): CipherTable? {
        val cipher = cipherRepository.findById(id).orElse(null) ?: return null
        if (cipher.owner != owner) return null
        return cipher
    }

    fun getAllCiphers(owner: UUID): List<UUID> = cipherRepository.getAllIds(owner)

    fun deleteCipher(id: UUID) {
        cipherRepository.deleteById(id)
    }
}
