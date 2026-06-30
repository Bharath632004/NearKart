package com.nearkart.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class UserProfileResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String profilePictureUrl;
    private String bio;
    private List<AddressResponse> addresses;
    private LocalDateTime createdAt;
}
