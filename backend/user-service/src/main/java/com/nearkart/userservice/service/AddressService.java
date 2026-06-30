package com.nearkart.userservice.service;

import com.nearkart.userservice.dto.AddressRequest;
import com.nearkart.userservice.exception.AddressNotFoundException;
import com.nearkart.userservice.exception.UserNotFoundException;
import com.nearkart.userservice.model.Address;
import com.nearkart.userservice.model.User;
import com.nearkart.userservice.repository.AddressRepository;
import com.nearkart.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public Address addAddress(String email, AddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (request.isDefault()) {
            addressRepository.clearDefaultForUser(user.getId());
        }

        Address address = new Address();
        address.setUserId(user.getId());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLandmark(request.getLandmark());
        address.setDefault(request.isDefault());

        return addressRepository.save(address);
    }

    public List<Address> getAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return addressRepository.findByUserId(user.getId());
    }

    public Address updateAddress(String email, Long addressId, AddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        if (!address.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied to this address");
        }

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

    public void deleteAddress(String email, Long addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        if (!address.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied to this address");
        }

        addressRepository.delete(address);
    }
}
