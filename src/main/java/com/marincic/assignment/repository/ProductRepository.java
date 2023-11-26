package com.marincic.assignment.repository;


import com.marincic.assignment.model.Product;

import java.util.Currency;
import java.util.List;

import static com.marincic.assignment.model.enumeration.ProductType.BEVERAGE;
import static com.marincic.assignment.model.enumeration.ProductType.EXTRAS;
import static com.marincic.assignment.model.enumeration.ProductType.SNACK;
import static java.util.Currency.getInstance;
import static java.util.Locale.forLanguageTag;

public class ProductRepository {

    public List<Product> findAllById(List<Integer> productIds) {
        return mockedProducts().stream()
                               .filter(product -> productIds.contains(product.id()))
                               .toList();
    }

    private static List<Product> mockedProducts() {
        Currency currency = getInstance(forLanguageTag("de-CH"));

        return List.of(
                new Product(1, "Coffee (small)", 2.50, currency, BEVERAGE),
                new Product(2, "Coffee (medium)", 3.00, currency, BEVERAGE),
                new Product(3, "Coffee (large)", 3.50, currency, BEVERAGE),
                new Product(4, "Bacon Roll", 4.50, currency, SNACK),
                new Product(5, "Freshly squeezed orange juice (0.25l)", 3.95, currency, BEVERAGE),
                new Product(6, "Extra milk", 0.30, currency, EXTRAS),
                new Product(7, "Foamed milk", 0.50, currency, EXTRAS),
                new Product(8, "Special roast coffee", 0.90, currency, EXTRAS)
        );
    }

}
