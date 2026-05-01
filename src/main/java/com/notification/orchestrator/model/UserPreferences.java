package com.notification.orchestrator.model;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {

    private String userId;
    private LocalTime dndStart;
    private LocalTime dndEnd;

}
