package com.marincic.assignment.service;

import com.marincic.assignment.model.Receipt;

import java.util.List;

public interface ReceiptService {

    Receipt createReceipt(Integer bonusCardId, List<Integer> productIds);

}
