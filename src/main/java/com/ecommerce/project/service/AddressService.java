package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;

/**
 * Service interface for managing and performing operations related to addresses.
 * Provides methods to create, retrieve, and delete address records,
 * with pagination and sorting functionalities.
 */
public interface AddressService {
    /**
     * Creates a new address and associates it with the given user.
     *
     * @param addressDTO the data transfer object representing the address details to be created
     * @param user the user to whom the address will be associated
     * @return the created AddressDTO containing the details of the newly created address
     */
    AddressDTO createAddress(AddressDTO addressDTO, User user);

    /**
     * Retrieves all addresses with support for pagination and sorting functionality.
     *
     * @param pageNumber the number of the page to retrieve, starting from 0
     * @param pageSize the number of addresses to include in each page
     * @param sortBy the field by which the addresses should be sorted
     * @param sortOrder the order of sorting, such as "asc" for ascending or "desc" for descending
     * @return an AddressResponse object containing a paginated list of addresses and related metadata
     */
    AddressResponse getAllAddresses(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    /**
     * Retrieves a paginated and sorted list of addresses associated with a specific user.
     *
     * @param pageNumber the number of the page to retrieve, starting from 0
     * @param pageSize the number of addresses to include in each page
     * @param sortBy the field by which the addresses should be sorted
     * @param sortOrder the sorting order, either "asc" for ascending or "desc" for descending
     * @param user_id the unique identifier of the user whose addresses are to be retrieved
     * @return an AddressResponse object containing a paginated list of addresses and related metadata
     */
    AddressResponse getAddresses(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Long user_id);

    /**
     * Removes an address associated with the specified user based on the given address ID.
     * If the address is successfully removed, returns the details of the removed address.
     *
     * @param addressId the unique identifier of the address to be removed
     * @param user the user associated with the address to be removed
     * @return the AddressDTO containing the details of the removed address
     */
    AddressDTO removeAddress(Long addressId, User user);
}
