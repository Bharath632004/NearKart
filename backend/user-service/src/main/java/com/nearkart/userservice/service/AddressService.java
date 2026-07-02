package com.nearkart.userservice.service;

import com.nearkart.userservice.dto.AddressRequest;
import com.nearkart.userservice.exception.AddressNotFoundException;
import com.nearkart.userservice.exception.UserNotFoundException;
import com.nearkart.userservice.model.Address;
import com.nearkart.userservice.model.User;
import com.nearkart.userservice.repository.AddressRepository;
import com.nearkart.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public Address addAddress(String email, AddressRequest request) {
        User user = findUserOrThrow(email);
        if (request.isDefault()) {
            addressRepository.clearDefaultForUser(user.getId());
        }
        Address address = Address.builder()
                .userId(user.getId())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .landmark(request.getLandmark())
                .isDefault(request.isDefault())
                .build();
        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<Address> getAddresses(String email) {
        User user = findUserOrThrow(email);
        return addressRepository.findByUserId(user.getId());
    }

    @Transactional
    public Address updateAddress(String email, Long addressId, AddressRequest request) {
        User user = findUserOrThrow(email);
        Address address = findAddressOrThrow(addressId);
        verifyOwnership(address, user);

        if (request.isDefault()) {
            addressRepository.clearDefaultForUser(user.getId());
        }
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLandmark(request.getLandmark());
        address.setDefault(request.isDefault());
        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = findUserOrThrow(email);
        Address address = findAddressOrThrow(addressId);
        verifyOwnership(address, user);
        addressRepository.delete(address);
        log.info("Address {} deleted for user {}", addressId, email);
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private Address findAddressOrThrow(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));
    }

    private void verifyOwnership(Address address, User user) {
        if (!address.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this address");
        }
    }
}
