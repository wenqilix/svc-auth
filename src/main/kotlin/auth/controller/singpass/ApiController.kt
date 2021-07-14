package auth.controller.singpass

import auth.service.singpass.LoginService
import auth.service.singpass.OpenIdTokenService
import org.jose4j.jwt.consumer.InvalidJwtException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("SingpassRestController")
@CrossOrigin
@RequestMapping("/sp")
class ApiController {
    private val logger = LoggerFactory.getLogger(ApiController::class.java)

    @Autowired
    lateinit var loginService: LoginService
    @Autowired
    lateinit var openIdTokenService: OpenIdTokenService

    companion object {
        const val HEADER_STRING = "Authorization"
    }

    @GetMapping("/refresh")
    fun refresh(@RequestHeader(value = HEADER_STRING) bearerToken: String): ResponseEntity<Map<String, Any?>> {
        val body = HashMap<String, Any?>()
        try {
            val token = bearerToken.replace("Bearer ", "")
            val headers = HttpHeaders()
            headers.add(HEADER_STRING, loginService.refreshToken(token))

            body.put("success", true)

            return ResponseEntity<Map<String, Any?>>(body, headers, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e.message, e)
            body.put("error", e.message)
            when (e) {
                is InvalidJwtException -> {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
                }
                else -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
                }
            }
        }
    }

    @GetMapping("/token")
    fun token(
        @RequestParam(value = "authCode") authCode: String
    ): ResponseEntity<Map<String, Any?>> {
        val body = HashMap<String, Any?>()
        try {
            val token = loginService.getToken(authCode)

            val headers = HttpHeaders()
            headers.add(HEADER_STRING, token)

            body.put("success", true)

            return ResponseEntity<Map<String, Any?>>(body, headers, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e.message, e)
            body.put("error", e.message)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
        }
    }

    @GetMapping("/openid/jwks")
    fun openIdJsonWebKeys(): ResponseEntity<Map<String, Any?>> {
        val body = HashMap<String, Any?>()
        try {
            val jwks = openIdTokenService.getJsonWebKeys()

            body.put("keys", jwks)

            return ResponseEntity<Map<String, Any?>>(body, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e.message, e)
            body.put("error", e.message)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
        }
    }
}
