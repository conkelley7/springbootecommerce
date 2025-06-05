package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;
import com.ecommerce.project.repositories.AddressRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {


    private final ModelMapper modelMapper;
    private final AddressRepository addressRepository;

    public AddressServiceImpl(ModelMapper modelMapper, AddressRepository addressRepository) {
        this.modelMapper = modelMapper;
        this.addressRepository = addressRepository;
    }

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);

        user.getAddresses().add(address);
        address.getUsers().add(user);

        addressRepository.save(address);

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public AddressResponse getAllAddresses(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        
        Page<Address> addresses = addressRepository.findAll(pageable);

        List<AddressDTO> addressesDTOs = addresses
                .stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();

        return new AddressResponse(
              addressesDTOs,
              addresses.getNumber(),
              addresses.getSize(),
              addresses.getTotalElements(),
                addresses.isLast()
        );
    }

    @Override
    public AddressResponse getAddresses(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Long userId) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Address> addresses = addressRepository.findAllByUserId(userId, pageable);

        List<AddressDTO> addressDTOS = addresses
                .stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();

        return new AddressResponse(
                addressDTOS,
                addresses.getNumber(),
                addresses.getSize(),
                addresses.getTotalElements(),
                addresses.isLast()
        );
    }

    @Override
    public AddressDTO removeAddress(Long addressId, User user) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new APIException("Address not found with ID: " + addressId));

        address.getUsers().remove(user);
        user.getAddresses().remove(address);

        addressRepository.save(address);

        return modelMapper.map(address, AddressDTO.class);
    }


}
