package com.ecommerce.project.service.address;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
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
        // Controleer eerst of het adres bestaat
        int houseNumber = addressDTO.getHouseNumber();
        String zipcode = addressDTO.getZipcode();
        String addressNumber = addressDTO.getApartmentNumber();
        Address address = addressRepository
                .findByHouseNumberAndZipcodeAndApartmentNumber(houseNumber, zipcode, addressNumber);

        if (address != null) {
            throw new APIException("Address already exists");
        }

        // Haal ingelogde user op
        User user = authUtils.getLoggedInUser();
        // Converteer de addressDTO naar een address object
        address = modelMapper.map(addressDTO, Address.class);

        // Haal de adressen op van de ingelogde user. Deze moet bijgewerkt worden zodat de link in de tussen tabel
        // aangemaakt kan worden
        Set<Address> addresses = user.getAddresses();
        // Voeg het nieuwe adres toe aan de lijst van adressen van de user
        addresses.add(address);
        // Sla de nieuwe lijst aan adressen op in het user object
        user.setAddresses(addresses);

        // Sla het adres op in de database
        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        // De owner van de many-to-many relatie (de user) moet eerst bijgewerkt worden. Dit betekent dat het address
        // eerst verwijderd dient te worden bij de user. Daarna kan het adres verwijderd worden uit de database
        User user = authUtils.getLoggedInUser();

        Set<Address> addresses = user.getAddresses();
        addresses.remove(address);
        user.setAddresses(addresses);

        addressRepository.delete(address);

        return "Address successfully deleted with addressId " + addressId;
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
