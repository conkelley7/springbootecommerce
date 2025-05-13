package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service interface for managing products.
 * Provides methods to create, retrieve, update, delete, and search for products.
 */
public interface ProductService {
    /**
     * Adds a new product to a specified category.
     * This method associates a product with an existing category and stores
     * the product details in the system.
     *
     * @param categoryId the unique identifier of the category to which the product belongs
     * @param product the product details provided as a data transfer object to be added to the category
     * @return the added product as a data transfer object containing the product's details
     */
    ProductDTO addProduct(Long categoryId, ProductDTO product);

    /**
     * Retrieves a paginated and sorted list of products along with metadata.
     *
     * @param pageNumber the page number to retrieve, starting from 0
     * @param pageSize the number of products to include on each page
     * @param sortBy the field to sort the products by, e.g., name, price
     * @param sortOrder the sort direction, either "asc" for ascending or "desc" for descending
     * @return a ProductResponse object containing a list of products and pagination details
     */
    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    /**
     * Searches for products within a specific category and retrieves a paginated,
     * sorted list of products along with relevant metadata.
     *
     * @param categoryId the unique identifier of the category in which to search for products
     * @param pageNumber the page number to retrieve, starting from 0
     * @param pageSize the number of products to include on each page
     * @param sortBy the field to sort the products by, e.g., name, price
     * @param sortOrder the sort direction, either "asc" for ascending or "desc" for descending
     * @return a ProductResponse object containing a list of products matching the category,
     *         as well as pagination and sorting details
     */
    ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    /**
     * Searches for products using a specified keyword and retrieves a paginated, sorted list
     * of products along with relevant metadata.
     *
     * @param keyword the keyword to search for in the product names or descriptions
     * @param pageNumber the page number to retrieve, starting from 0
     * @param pageSize the number of products to include on each page
     * @param sortBy the field to sort the products by, e.g., name, price
     * @param sortOrder the sort direction, either "asc" for ascending or "desc" for descending
     * @return a ProductResponse object containing a list of matching products,
     *         as well as pagination and sorting details
     */
    ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    /**
     * Updates the details of an existing product identified by its ID.
     * The updated details are provided as a data transfer object (DTO).
     *
     * @param productId the unique identifier of the product to be updated
     * @param product the data transfer object containing the updated details of the product
     * @return the updated product as a data transfer object
     */
    ProductDTO updateProduct(Long productId, ProductDTO product);

    /**
     * Deletes a product from the system based on its unique identifier.
     * If the product with the specified ID does not exist, an exception is thrown.
     *
     * @param productId the unique identifier of the product to be deleted
     * @return the data transfer object representing the deleted product
     */
    ProductDTO deleteProduct(Long productId);

    /**
     * Updates the image associated with a product identified by its ID.
     * The provided image is uploaded and the product details are updated
     * with the new image file name.
     *
     * @param productId the unique identifier of the product whose image needs to be updated
     * @param image the image file to be associated with the product, provided as a MultipartFile
     * @return the updated product details as a ProductDTO
     * @throws IOException if an error occurs during the image upload process
     */
    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;
}
