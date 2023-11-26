package com.marincic.assignment.service;

import com.marincic.assignment.model.Product;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BonusService {

    List<Product> getDiscountsFromBonusCard(Integer bonusCardId,
                                            List<Integer> productIds,
                                            Map<Integer, Product> orderedProductsMap,
                                            Map<Integer, Long> orderedProductsQuantity);

    Optional<Product> getFreeExtra(Integer bonusCardId,
                                   List<Integer> productIds,
                                   Map<Integer, Product> orderedProductsMap,
                                   Map<Integer, Long> orderedProductsQuantity);

}
