package auth

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus

@RestController("RestController")
@CrossOrigin
class ApiController {
    @GetMapping("/version")
    fun version(): ResponseEntity<Map<String, Any?>> {
        val body = HashMap<String, Any?>()
        try {
            val version = javaClass.classLoader.getResource("static/.version").readText()

            body.put("version", version)

            return ResponseEntity<Map<String, Any?>>(body, HttpStatus.OK)
        } catch (e: Exception) {
            System.out.println(e)
            var httpStatus = HttpStatus.NOT_IMPLEMENTED
            return ResponseEntity.status(httpStatus).body(body)
        }
    }
}
