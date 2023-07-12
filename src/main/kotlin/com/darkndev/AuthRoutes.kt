package com.darkndev

import com.darkndev.data.UserDao
import com.darkndev.models.AuthRequest
import com.darkndev.models.AuthResponse
import com.darkndev.models.User
import com.darkndev.security.hashing.SHA256HashingService
import com.darkndev.security.hashing.SaltedHash
import com.darkndev.security.token.JwtTokenService
import com.darkndev.security.token.TokenClaim
import com.darkndev.security.token.TokenConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.signUp(
    hashingService: SHA256HashingService
) {
    val userDao = UserDao()
    post("/signup") {
        val request = call.receive<AuthRequest>()

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()

        val isPasswordTooShort = request.password.length < 8

        if (areFieldsBlank || isPasswordTooShort) {
            call.respond(HttpStatusCode.Conflict, "Check the Fields")
            return@post
        }

        val existingUser = userDao.getUserByUsername(request.username)
        if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict, "User Exists")
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = userDao.insertUser(request.username, saltedHash.hash, saltedHash.salt)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Server error")
            return@post
        }
        call.respond(HttpStatusCode.OK, "User Created")
    }
}

fun Route.signIn(
    hashingService: SHA256HashingService,
    tokenService: JwtTokenService,
    tokenConfig: TokenConfig
) {
    val userDao = UserDao()
    post("/signin") {
        val request = call.receive<AuthRequest>()

        val user = userDao.getUserByUsername(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(HttpStatusCode.OK, AuthResponse(token))
    }
}

fun Route.authenticate() {
    authenticate {
        get("/authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("/secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}

fun Route.allUsers() {
    val userDao = UserDao()
    get("/users") {
        val users = userDao.allUsers()
        call.respond(HttpStatusCode.OK, users)
    }
}