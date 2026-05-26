package com.tracker.alcohol.controller;

import com.tracker.alcohol.model.DrinkEvent;
import com.tracker.alcohol.model.User;
import com.tracker.alcohol.repository.DrinkEventRepository;
import com.tracker.alcohol.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drinks")
public class DrinkEventController {

    private final DrinkEventRepository drinkEventRepository;
    private final UserRepository userRepository;

    @Autowired
    public DrinkEventController(DrinkEventRepository drinkEventRepository, UserRepository userRepository) {
        this.drinkEventRepository = drinkEventRepository;
        this.userRepository = userRepository;
    }

    // GET all drinks for logged-in user
    @GetMapping
    public List<DrinkEvent> getAllDrinks(Principal principal) {
        User user = getUser(principal);
        return drinkEventRepository.findByUserIdOrderByConsumedAtDesc(user.getId());
    }

    // GET drinks with optional filters (date range + beverage type)
    @GetMapping("/filter")
    public List<DrinkEvent> filterDrinks(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String type,
            Principal principal) {

        User user = getUser(principal);
        LocalDateTime startDate = (from != null && !from.isEmpty())
                ? LocalDateTime.parse(from + "T00:00:00") : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endDate = (to != null && !to.isEmpty())
                ? LocalDateTime.parse(to + "T23:59:59") : LocalDateTime.now();

        if (type != null && !type.isEmpty()) {
            return drinkEventRepository
                    .findByUserIdAndConsumedAtBetweenAndBeverageNameContainingIgnoreCaseOrderByConsumedAtDesc(
                            user.getId(), startDate, endDate, type);
        } else {
            return drinkEventRepository
                    .findByUserIdAndConsumedAtBetweenOrderByConsumedAtDesc(
                            user.getId(), startDate, endDate);
        }
    }

    // CREATE a new drink
    @PostMapping
    public ResponseEntity<DrinkEvent> addDrink(@RequestBody Map<String, Object> payload, Principal principal) {
        User user = getUser(principal);

        DrinkEvent drink = new DrinkEvent();
        drink.setUser(user);
        drink.setBeverageName((String) payload.get("beverageName"));
        drink.setQuantity(Double.parseDouble(payload.get("quantity").toString()));
        drink.setSourceType(payload.getOrDefault("sourceType", "MANUAL").toString());
        drink.setNotes((String) payload.getOrDefault("notes", ""));
        drink.setConsumedAt(LocalDateTime.now());

        DrinkEvent saved = drinkEventRepository.save(drink);
        return ResponseEntity.ok(saved);
    }

    // UPDATE an existing drink
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDrink(@PathVariable Long id,
                                         @RequestBody Map<String, Object> payload,
                                         Principal principal) {
        User user = getUser(principal);

        DrinkEvent drink = drinkEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drink not found"));

        // Security check: make sure user owns this drink
        if (!drink.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        if (payload.containsKey("beverageName")) {
            drink.setBeverageName((String) payload.get("beverageName"));
        }
        if (payload.containsKey("quantity")) {
            drink.setQuantity(Double.parseDouble(payload.get("quantity").toString()));
        }
        if (payload.containsKey("notes")) {
            drink.setNotes((String) payload.get("notes"));
        }

        DrinkEvent saved = drinkEventRepository.save(drink);
        return ResponseEntity.ok(saved);
    }

    // DELETE a drink
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDrink(@PathVariable Long id, Principal principal) {
        User user = getUser(principal);

        DrinkEvent drink = drinkEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drink not found"));

        // Security check: make sure user owns this drink
        if (!drink.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        drinkEventRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Drink deleted successfully"));
    }

    private User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
