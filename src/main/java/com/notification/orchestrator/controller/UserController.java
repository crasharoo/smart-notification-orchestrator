package com.notification.orchestrator.controller;

import com.notification.orchestrator.model.UserPreferences;
import com.notification.orchestrator.service.UserPreferenceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserPreferenceService service;

    public UserController(UserPreferenceService service) {
        this.service = service;
    }

    @PostMapping("/preferences")
    public String savePreferences(@RequestBody UserPreferences preferences) {
        service.savePreferences(preferences);
        return "Preferences saved";
    }
}
