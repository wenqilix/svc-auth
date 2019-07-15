package auth.singpass

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.jose4j.jwt.consumer.InvalidJwtException
import auth.helper.Jwt
import auth.singpass.model.User

@RestController("singpassRestController")
@CrossOrigin
@RequestMapping("/sp")
class ApiController {
    @Autowired
    lateinit var jwt: Jwt

    companion object {
        const val HEADER_STRING = "Authorization"
    }

    @GetMapping("/refresh")
    fun refresh(@RequestHeader(value = HEADER_STRING) bearerToken: String): ResponseEntity<Map<String, Any?>> {
        val body = HashMap<String, Any?>()
        try {
            val token = bearerToken.replace("Bearer ", "")
            val attributes: Map<String, Any> = jwt.parseSingpass(token)
            val user = User(attributes)

            val headers = HttpHeaders()
            headers.add(HEADER_STRING, jwt.buildSingpass(user.toMap()))

            body.put("success", true)

            return ResponseEntity<Map<String, Any?>>(body, headers, HttpStatus.OK)
        } catch (e: Exception) {
            var httpStatus = HttpStatus.BAD_REQUEST
            when (e) {
                is InvalidJwtException -> {
                    httpStatus = HttpStatus.UNAUTHORIZED
                }
            }

            body.put("error", e.message)
            return ResponseEntity.status(httpStatus).body(body)
        }
    }
}
