package com.tracker.alcohol.controller;

import com.tracker.alcohol.model.User;
import com.tracker.alcohol.model.UserSettings;
import com.tracker.alcohol.repository.UserRepository;
import com.tracker.alcohol.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private final UserSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserSettingsController(UserSettingsRepository settingsRepository, UserRepository userRepository) {
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public UserSettings getSettings(Principal principal) {
        User user = getUser(principal);
        return settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    // Create default settings for the user
                    UserSettings defaults = new UserSettings(user, 14.0);
                    return settingsRepository.save(defaults);
                });
    }

    @PostMapping
    public ResponseEntity<UserSettings> updateSettings(@RequestBody Map<String, Object> payload,
                                                       Principal principal) {
        User user = getUser(principal);
        Double weeklyGoal = Double.parseDouble(payload.get("weeklyGoal").toString());

        UserSettings settings = settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> new UserSettings(user, 14.0));

        settings.setWeeklyGoal(weeklyGoal);
        UserSettings saved = settingsRepository.save(settings);
        return ResponseEntity.ok(saved);
    }

    private User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
