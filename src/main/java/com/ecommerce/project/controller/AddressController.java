package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.address.IAddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/auth")
public class AddressController {
    @Autowired
    private IAddressService addressService;

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        List<AddressDTO> addressDTOs = addressService.getAllAddresses();
        return new ResponseEntity<>(addressDTOs, HttpStatus.OK);
    }

    @PostMapping("/address")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO savedAddressDTO = addressService.createAddress(addressDTO);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO> deleteAddress(@PathVariable Long addressId) {
        AddressDTO addressDTO = addressService.deleteAddress(addressId);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }
}
