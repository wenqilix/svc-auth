package auth.controller

import auth.service.TokenService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("RestController")
@CrossOrigin
class ApiController {
    private val logger = LoggerFactory.getLogger(ApiController::class.java)

    @Autowired
    lateinit var tokenService: TokenService

    @GetMapping("/version")
    fun version(): ResponseEntity<Map<String, Any?>> {
        val body = HashMap<String, Any?>()
        try {
            val versionFile = javaClass.classLoader.getResource("static/.version")
            if (versionFile == null) {
                throw NullPointerException("static/.version file not found")
            }
            val version = versionFile.readText()

            body.put("version", version)

            return ResponseEntity<Map<String, Any?>>(body, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e.message, e)
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(body)
        }
    }

    @GetMapping("/jwks")
    fun jsonWebKeys(): ResponseEntity<Map<String, Any?>> {
        val body = HashMap<String, Any?>()
        try {
            val jwks = tokenService.getJsonWebKeys()

            body.put("keys", jwks)

            return ResponseEntity<Map<String, Any?>>(body, HttpStatus.OK)
        } catch (e: Exception) {
            logger.error(e.message, e)
            body.put("error", e.message)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
        }
    }
}
