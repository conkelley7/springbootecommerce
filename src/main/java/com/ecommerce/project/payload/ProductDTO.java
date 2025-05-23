package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;

    @NotBlank
    private String productName;

    @NotBlank
    private String description;

    private String image;

    private Integer quantity;

    @NotNull
    private double price;

    private double discount;

    private double specialPrice;
}
