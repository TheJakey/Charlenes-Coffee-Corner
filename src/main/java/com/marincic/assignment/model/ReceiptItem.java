package com.marincic.assignment.model;

import java.util.Currency;

public record ReceiptItem(
        String name,
        Double price,
        Long quantity,
        Double totalPrice,
        Currency currency
) {
}
