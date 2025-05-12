package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;


/**
 * Service interface for managing categories in the system.
 * Provides methods to retrieve, create, update, and delete categories.
 */
public interface CategoryService {

    /**
     * Retrieves a paginated and sorted list of categories along with metadata.
     *
     * @param pageNumber the page number to retrieve, starting from 0
     * @param pageSize the number of categories to include on each page
     * @param sortBy the field to sort the categories by
     * @param sortOrder the sort direction, either "asc" or "desc"
     * @return a CategoryResponse object containing a list of categories and pagination details
     */
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    /**
     * Creates a new category in the system.
     *
     * @param categoryDTO the data transfer object containing the details of the category to be created
     * @return the created category as a data transfer object
     */
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    /**
     * Deletes an existing category from the system based on the given category ID.
     * If the category with the specified ID does not exist, an exception is thrown.
     *
     * @param categoryId the unique identifier of the category to be deleted
     * @return the data transfer object representing the deleted category
     */
    CategoryDTO deleteCategory(Long categoryId);

    /**
     * Updates the details of an existing category identified by its ID.
     * The updated information is provided as a data transfer object (DTO).
     *
     * @param categoryDTO the data transfer object containing the new details of the category
     * @param categoryId the unique identifier of the category to be updated
     * @return the updated category as a data transfer object
     */
    CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);
}
