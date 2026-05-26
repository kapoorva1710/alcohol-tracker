package com.tracker.alcohol.controller;

import com.tracker.alcohol.model.DrinkEvent;
import com.tracker.alcohol.model.User;
import com.tracker.alcohol.repository.DrinkEventRepository;
import com.tracker.alcohol.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final DrinkEventRepository drinkEventRepository;
    private final UserRepository userRepository;

    @Autowired
    public WebhookController(DrinkEventRepository drinkEventRepository, UserRepository userRepository) {
        this.drinkEventRepository = drinkEventRepository;
        this.userRepository = userRepository;
    }

    // Endpoint for mobile automation (Tasker/Shortcuts) to forward SMS messages
    @PostMapping("/messages")
    public ResponseEntity<String> handleMessageWebhook(@RequestBody Map<String, String> payload) {
        String messageBody = payload.getOrDefault("message", "").toLowerCase();

        // Simple NLP: Check for keywords in the text message
        if (messageBody.contains("beer") || messageBody.contains("wine") || messageBody.contains("drink")) {
            Optional<User> userOpt = userRepository.findById(1L);
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok("No registered user found. Skipping log.");
            }

            DrinkEvent event = new DrinkEvent();
            event.setUser(userOpt.get());
            event.setBeverageName("Detected via Message");
            event.setQuantity(1.0);
            event.setSourceType("MESSAGE");
            event.setConsumedAt(LocalDateTime.now());
            event.setNotes(payload.getOrDefault("message", ""));

            drinkEventRepository.save(event);
            return ResponseEntity.ok("Message processed and drink logged.");
        }

        return ResponseEntity.ok("Message ignored (no alcohol keywords detected).");
    }

    // Endpoint for Health App triggers (e.g., Apple Health via Shortcuts)
    @PostMapping("/health")
    public ResponseEntity<String> handleHealthWebhook(@RequestBody Map<String, Object> payload) {
        String eventType = (String) payload.getOrDefault("eventType", "");

        if ("LATE_NIGHT_ACTIVITY".equals(eventType) || "POOR_SLEEP_HRV".equals(eventType)) {
            Optional<User> userOpt = userRepository.findById(1L);
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok("No registered user found. Skipping log.");
            }

            DrinkEvent event = new DrinkEvent();
            event.setUser(userOpt.get());
            event.setBeverageName("Suspected Drink (Health Data)");
            event.setQuantity(1.0);
            event.setSourceType("HEALTH_APP");
            event.setConsumedAt(LocalDateTime.now());
            event.setNotes("Triggered by health event: " + eventType);

            drinkEventRepository.save(event);
            return ResponseEntity.ok("Health event processed and suspected drink logged.");
        }

        return ResponseEntity.ok("Health event ignored.");
    }
}
