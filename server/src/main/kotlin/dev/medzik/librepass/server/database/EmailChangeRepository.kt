package dev.medzik.librepass.server.database

import org.springframework.data.repository.CrudRepository
import java.util.UUID

/** Repository for the [EmailChangeTable] */
interface EmailChangeRepository : CrudRepository<EmailChangeTable, UUID>
