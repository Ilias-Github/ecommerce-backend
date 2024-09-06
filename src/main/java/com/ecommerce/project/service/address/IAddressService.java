package com.ecommerce.project.service.address;

import com.ecommerce.project.payload.AddressDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IAddressService {
    AddressDTO createAddress(AddressDTO addressDTO);

    String deleteAddress(Long addressId);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getAllAddressesByUser();

    AddressDTO updateAddress(AddressDTO addressDTO, Long addressId);
}
