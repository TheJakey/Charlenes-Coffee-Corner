package com.marincic.assignment.model;

import com.marincic.assignment.model.enumeration.ProductType;

import java.util.Currency;

public record Product(
        Integer id,
        String name,
        Double price, // TODO: Use BigDecimal instead
        Currency currency,
        ProductType type
) {
}
