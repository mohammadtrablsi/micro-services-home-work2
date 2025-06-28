@RestController
public class FallbackController {

    @RequestMapping("/fallback/user")
    public ResponseEntity<String> userFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("User service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/fallback/course")
    public ResponseEntity<String> courseFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Course service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/fallback/payment")
    public ResponseEntity<String> paymentFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Payment service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/fallback/assessment")
    public ResponseEntity<String> assessmentFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Assessment service is currently unavailable. Please try again later.");
    }
}
