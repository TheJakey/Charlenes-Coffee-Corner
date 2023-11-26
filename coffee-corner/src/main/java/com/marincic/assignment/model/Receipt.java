package com.marincic.assignment.model;

import java.util.List;
import java.util.UUID;

public record Receipt(
        UUID id,
        Integer bonusCardId,
        List<ReceiptItem> receiptItems
) {
}
