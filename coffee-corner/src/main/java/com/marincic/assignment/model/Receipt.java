package com.marincic.assignment.model;

import java.util.List;

public record Receipt(
        Integer id,
        Integer bonusCardId,
        List<ReceiptItem> receiptItems
) {
}
