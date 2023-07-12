package com.darkndev.plugins

import com.darkndev.*
import com.darkndev.security.hashing.SHA256HashingService
import com.darkndev.security.token.JwtTokenService
import com.darkndev.security.token.TokenConfig
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureRouting(
    hashingService: SHA256HashingService,
    tokenService: JwtTokenService,
    tokenConfig: TokenConfig
) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        signIn(hashingService, tokenService, tokenConfig)
        signUp(hashingService)
        authenticate()
        getSecretInfo()
        allUsers()
    }
}
