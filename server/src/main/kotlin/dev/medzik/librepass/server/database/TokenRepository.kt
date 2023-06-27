package dev.medzik.librepass.server.database

import org.springframework.data.repository.CrudRepository

interface TokenRepository : CrudRepository<TokenTable, String>
