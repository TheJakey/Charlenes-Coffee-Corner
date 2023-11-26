package com.marincic.assignment.service.impl;

import com.marincic.assignment.model.Product;
import com.marincic.assignment.model.Receipt;
import com.marincic.assignment.model.ReceiptItem;
import com.marincic.assignment.repository.ProductRepository;
import com.marincic.assignment.service.BonusService;
import com.marincic.assignment.service.ReceiptService;

import java.util.List;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ReceiptServiceImpl implements ReceiptService {

    private final ProductRepository productRepository;
    private final BonusService bonusService;

    public ReceiptServiceImpl(ProductRepository productRepository,
                              BonusService bonusService) {
        this.productRepository = productRepository;
        this.bonusService = bonusService;
    }

    @Override
    public Receipt createReceipt(Integer bonusCardId, List<Integer> productIds) {
        if (productIds.isEmpty()) {
            throw new IllegalArgumentException("Product IDs list cannot be empty");
        }

        Map<Integer, Long> orderedProductsQuantity = productIds.stream()
                                                               .collect(groupingBy(identity(), counting()));
        Map<Integer, Product> orderedProductsMap = productRepository.findAllById(productIds)
                                                                    .stream()
                                                                    .collect(toMap(Product::id,
                                                                                   identity()));

        if (orderedProductsMap.size() != orderedProductsQuantity.keySet().size()) {
            throw new IllegalArgumentException("Some of the products do not exist");
        }

        List<ReceiptItem> receiptItems = mapProductsToReceiptItems(orderedProductsMap, orderedProductsQuantity);

        bonusService.getDiscountsFromBonusCard(bonusCardId, productIds, orderedProductsMap, orderedProductsQuantity)
                    .stream()
                    .map(product -> mapProductToReceiptItem(product, 1L))
                    .forEach(receiptItems::add);

        bonusService.getFreeExtra(bonusCardId, productIds, orderedProductsMap, orderedProductsQuantity)
                    .map(product -> mapProductToReceiptItem(product, 1L))
                    .ifPresent(receiptItems::add);

        // This would be replaced by database `save()` method which would handle also the id generation
        return new Receipt(randomUUID(), bonusCardId, receiptItems);
    }

    private List<ReceiptItem> mapProductsToReceiptItems(Map<Integer, Product> orderedProductsMap,
                                                        Map<Integer, Long> orderedProductsQuantity) {
        return orderedProductsMap.values()
                                 .stream()
                                 .map(product -> mapProductToReceiptItem(product,
                                                                         orderedProductsQuantity.get(product.id())))
                                 .collect(toList());
    }

    private ReceiptItem mapProductToReceiptItem(Product product, Long quantity) {
        return new ReceiptItem(product.name(),
                               product.price(),
                               quantity,
                               quantity * product.price(),
                               product.currency());
    }

}
