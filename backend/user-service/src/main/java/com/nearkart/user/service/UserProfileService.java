package com.nearkart.user.service;

import com.nearkart.user.dto.*;
import com.nearkart.user.entity.Address;
import com.nearkart.user.entity.UserProfile;
import com.nearkart.user.kafka.UserEventProducer;
import com.nearkart.user.repository.AddressRepository;
import com.nearkart.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository profileRepo;
    private final AddressRepository addressRepo;
    private final UserEventProducer userEventProducer;

    public UserProfileResponse getProfile(UUID userId) {
        UserProfile profile = findOrThrow(userId);
        return toResponse(profile);
    }

    @Transactional
    public UserProfileResponse createProfile(UUID userId, String email, String fullName, String phone) {
        if (profileRepo.existsById(userId)) {
            return toResponse(findOrThrow(userId));
        }
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .phone(phone)
                .build();
        profileRepo.save(profile);
        log.info("Created profile for user: {}", userId);
        userEventProducer.publishUserRegistered(userId.toString(), email, fullName, phone);
        return toResponse(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile profile = findOrThrow(userId);
        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getProfilePictureUrl() != null) profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profileRepo.save(profile);
        return toResponse(profile);
    }

    @Transactional
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        UserProfile profile = findOrThrow(userId);
        if (request.isDefaultAddress()) {
            addressRepo.clearDefaultForUser(userId);
        }
        Address address = Address.builder()
                .userProfile(profile)
                .label(request.getLabel())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .defaultAddress(request.isDefaultAddress())
                .build();
        addressRepo.save(address);
        return toAddressResponse(address);
    }

    public List<AddressResponse> getAddresses(UUID userId) {
        return addressRepo.findByUserProfileUserId(userId)
                .stream().map(this::toAddressResponse).toList();
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
        if (!address.getUserProfile().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: address belongs to another user");
        }
        addressRepo.delete(address);
    }

    private UserProfile findOrThrow(UUID userId) {
        return profileRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));
    }

    private UserProfileResponse toResponse(UserProfile p) {
        return UserProfileResponse.builder()
                .userId(p.getUserId())
                .fullName(p.getFullName())
                .email(p.getEmail())
                .phone(p.getPhone())
                .profilePictureUrl(p.getProfilePictureUrl())
                .bio(p.getBio())
                .addresses(p.getAddresses().stream().map(this::toAddressResponse).toList())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private AddressResponse toAddressResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .label(a.getLabel())
                .addressLine1(a.getAddressLine1())
                .addressLine2(a.getAddressLine2())
                .city(a.getCity())
                .state(a.getState())
                .pincode(a.getPincode())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .defaultAddress(a.isDefaultAddress())
                .build();
    }
}
