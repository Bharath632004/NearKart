package com.nearkart.user.controller;

import com.nearkart.user.dto.*;
import com.nearkart.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Manage user profiles and addresses")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication auth,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "Get all saved addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(profileService.getAddresses(userId));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add a new address")
    public ResponseEntity<AddressResponse> addAddress(
            Authentication auth,
            @Valid @RequestBody AddressRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addAddress(userId, request));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @Operation(summary = "Delete a saved address")
    public ResponseEntity<Void> deleteAddress(
            Authentication auth,
            @PathVariable UUID addressId) {
        UUID userId = UUID.fromString(auth.getName());
        profileService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    // Internal endpoint called by auth-service after registration
    @PostMapping("/internal/create")
    @Operation(summary = "Internal: Create profile after registration")
    public ResponseEntity<UserProfileResponse> createProfile(
            @RequestParam UUID userId,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createProfile(userId, email, fullName, phone));
    }
}
