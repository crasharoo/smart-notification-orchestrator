package com.notification.orchestrator.service;

import com.notification.orchestrator.model.UserPreferences;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserPreferenceService {

    private final ConcurrentHashMap<String, UserPreferences> store =
            new ConcurrentHashMap<>();

    public UserPreferences getUserPreferences(String userId) {
        return store.getOrDefault(userId,
                UserPreferences.builder()
                        .userId(userId)
                        .dndStart(LocalTime.of(23, 0)) // 11pm
                        .dndEnd(LocalTime.of(7, 0))    // 7am
                        .build());
    }

    public void savePreferences(UserPreferences userPreferences) {
        store.put(userPreferences.getUserId(), userPreferences);
    }

}
