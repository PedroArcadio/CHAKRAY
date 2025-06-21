package com.api.examen.services;

import com.api.examen.entities.Address;
import com.api.examen.repositories.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> findAllAddressById(int userId) {
        return addressRepository.findAllAddressById(userId);
    }

    public Optional<Address> findById(int addressId) {
        return addressRepository.findById(addressId);
    }

    public void save (Address address) {
        addressRepository.save(address);
    }

}
