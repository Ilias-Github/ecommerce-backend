package com.ecommerce.project.service.address;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.IAddressRepository;
import com.ecommerce.project.util.AuthUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AddressServiceImpl implements IAddressService {
    @Autowired
    IAddressRepository addressRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtils authUtils;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        int houseNumber = addressDTO.getHouseNumber();
        String zipcode = addressDTO.getZipcode();
        String addressNumber = addressDTO.getApartmentNumber();
        Address address = addressRepository
                .findByHouseNumberAndZipcodeAndApartmentNumber(houseNumber, zipcode, addressNumber);

        if (address != null) {
            throw new APIException("Address already exists");
        }

        address = new Address();
        address.setStreetName(addressDTO.getStreetName());
        address.setHouseNumber(addressDTO.getHouseNumber());
        address.setApartmentNumber(addressDTO.getApartmentNumber());
        address.setZipcode(addressDTO.getZipcode());
        address.setCity(addressDTO.getCity());

        addressRepository.save(address);

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public AddressDTO deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        addressRepository.delete(address);

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();

        if (addresses.isEmpty()) {
            throw new APIException("No addresses exist yet");
        }

        return addresses.stream().map(address -> modelMapper.map(address, AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddressesByUser() {
        Set<Address> addresses = authUtils.getLoggedInUser().getAddresses();

        System.out.println(addresses);
        System.out.println(authUtils.getLoggedInUser());

        if (addresses.isEmpty()) {
            throw new APIException("User doesn't have any addresses yet");
        }

        return addresses.stream().map(address -> modelMapper.map(address, AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        address.setStreetName(addressDTO.getStreetName());
        address.setHouseNumber(addressDTO.getHouseNumber());
        address.setZipcode(addressDTO.getZipcode());
        address.setCity(addressDTO.getCity());
        address.setApartmentNumber(addressDTO.getApartmentNumber());

        addressRepository.save(address);

        return modelMapper.map(address, AddressDTO.class);
    }
}
