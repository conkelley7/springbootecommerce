package com.ecommerce.project.controller;


import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;
import com.ecommerce.project.security.service.AuthUtil;
import com.ecommerce.project.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AuthUtil authUtil;

    @Autowired
    AddressService addressService;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@RequestBody AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        AddressDTO savedAddressDTO = addressService.createAddress(addressDTO, user);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @GetMapping("/addresses/all")
    public ResponseEntity<AddressResponse> getAllAddresses(
            @RequestParam(name = "PageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "PageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "SortBy", defaultValue = AppConstants.SORT_ADDRESSES_BY, required = false) String sortBy,
            @RequestParam(name = "SortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        AddressResponse response = addressService.getAllAddresses(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/addresses")
    public ResponseEntity<AddressResponse> getAddressesForLoggedInUser(
            @RequestParam(name = "PageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "PageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "SortBy", defaultValue = AppConstants.SORT_ADDRESSES_BY, required = false) String sortBy,
            @RequestParam(name = "SortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        Long userId = authUtil.loggedInUserId();
        AddressResponse response = addressService.getAddresses(pageNumber, pageSize, sortBy, sortOrder, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> removeAddressFromLoggedInUser(@PathVariable Long addressId) {
        User user = authUtil.loggedInUser();
        AddressDTO addressDTO = addressService.removeAddress(addressId, user);

        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }
}
