package com.darkndev

import com.darkndev.data.DatabaseFactory
import io.ktor.server.application.*
import com.darkndev.plugins.*
import com.darkndev.security.hashing.SHA256HashingService
import com.darkndev.security.token.JwtTokenService
import com.darkndev.security.token.TokenConfig
import java.time.Duration

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    DatabaseFactory.init(environment.config)

    val tokenService = JwtTokenService()

    //Must set token expiresIn, environment variable for jwt-secret and issuer and audience in application config
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = Duration.ofHours(1).toMillis(),
        secret = System.getenv("JWT_SECRET")
    )

    val hashingService = SHA256HashingService()

    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(hashingService, tokenService, tokenConfig)
}
