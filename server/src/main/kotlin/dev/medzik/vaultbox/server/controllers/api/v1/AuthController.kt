package dev.medzik.vaultbox.server.controllers.api.v1

import dev.medzik.vaultbox.server.services.UserService
import dev.medzik.vaultbox.server.utils.Response
import dev.medzik.vaultbox.server.utils.ResponseError
import dev.medzik.vaultbox.server.utils.ResponseHandler
import dev.medzik.vaultbox.types.api.auth.LoginRequest
import dev.medzik.vaultbox.types.api.auth.RefreshRequest
import dev.medzik.vaultbox.types.api.auth.RegisterRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {
    @Autowired
    private lateinit var userService: UserService

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): Response {
        userService.register(request.email, request.password, request.passwordHint, request.encryptionKey)

        // TODO: send email with verification link

        return ResponseHandler.generateResponse(HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Response {
        val credentials = userService.login(request.email, request.password) ?: return ResponseError.InvalidCredentials
        return ResponseHandler.generateResponse(credentials, HttpStatus.OK)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): Response {
        val credentials = userService.refreshToken(request.refreshToken) ?: return ResponseError.InvalidCredentials
        return ResponseHandler.generateResponse(credentials, HttpStatus.OK)
    }
}
