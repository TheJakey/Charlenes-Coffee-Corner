package com.marincic.assignment.model;

import java.util.Currency;

public record ReceiptItem(
        String name,
        Double price,
        int quantity,
        Double totalPrice,
        Currency currency
) {
}
