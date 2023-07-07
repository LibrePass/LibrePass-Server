package dev.medzik.librepass.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}
