package com.marincic.assignment.service.impl;

import com.marincic.assignment.model.BonusCard;
import com.marincic.assignment.model.Product;
import com.marincic.assignment.model.Receipt;
import com.marincic.assignment.model.ReceiptItem;
import com.marincic.assignment.model.enumeration.ProductType;
import com.marincic.assignment.repository.BonusCardRepository;
import com.marincic.assignment.repository.ProductRepository;
import com.marincic.assignment.service.ReceiptService;

import java.util.List;
import java.util.Map;

import static com.marincic.assignment.model.enumeration.ProductType.BEVERAGE;
import static com.marincic.assignment.model.enumeration.ProductType.EXTRAS;
import static com.marincic.assignment.model.enumeration.ProductType.SNACK;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ReceiptServiceImpl implements ReceiptService {

    private final ProductRepository productRepository = new ProductRepository();
    private final BonusCardRepository bonusCardRepository = new BonusCardRepository();

    @Override
    public Receipt createReceipt(Integer bonusCardId, List<Integer> productIds) {
        Map<Integer, Long> orderedProductsQuantity = productIds.stream()
                                                               .collect(groupingBy(integer -> integer, counting()));
        Map<Integer, Product> orderedProductsMap = productRepository.findAllById(productIds)
                                                                    .stream()
                                                                    .collect(toMap(Product::id,
                                                                                   identity()));

        List<ReceiptItem> receiptItems = mapProductsToReceiptItems(orderedProductsMap, orderedProductsQuantity);

        int beverageCount = getNumberOfProductsWithGivenType(orderedProductsMap, orderedProductsQuantity, BEVERAGE);

        BonusCard bonusCard = bonusCardRepository.findById(bonusCardId);
        if (nonNull(bonusCard)) {
            int numberOfFreeBeveragesAlreadyClaimed = bonusCard.numberOfBeveragesPurchased() / 5;

            BonusCard newBonusCardState = new BonusCard(bonusCard.id(),
                                                        bonusCard.numberOfBeveragesPurchased() + beverageCount);

            int numberOfFreeBeveragesToBeClaimed = newBonusCardState.numberOfBeveragesPurchased() / 5 - numberOfFreeBeveragesAlreadyClaimed;

            List<Product> orderedBeveragesByPrice = productIds.stream()
                                                              .map(orderedProductsMap::get)
                                                              .filter(product -> product.type().equals(BEVERAGE))
                                                              .sorted(comparing(Product::price))
                                                              .toList();

            for (int i = 0; i < numberOfFreeBeveragesToBeClaimed; i++) {
                Product product = orderedBeveragesByPrice.get(i);

                if (nonNull(product)) {
                    Product beverageToBeFree = new Product(product.id(),
                                                           product.name(),
                                                           product.price() * -1,
                                                           product.currency(),
                                                           product.type());

                    receiptItems.add(mapProductToReceiptItem(beverageToBeFree, 1));
                }
            }
        }

        int snacksCount = getNumberOfProductsWithGivenType(orderedProductsMap, orderedProductsQuantity, SNACK);
        if (beverageCount > 0 && snacksCount > 0) {
            Product extraToBeFree = orderedProductsMap.values()
                                                      .stream()
                                                      .filter(product -> product.type().equals(EXTRAS))
                                                      .min(comparing(Product::price))
                                                      .map(product -> new Product(product.id(),
                                                                                  product.name(),
                                                                                  product.price() * -1,
                                                                                  product.currency(),
                                                                                  product.type()))
                                                      .orElse(null);

            if (nonNull(extraToBeFree)) {
                receiptItems.add(mapProductToReceiptItem(extraToBeFree, 1));
            }
        }

        // This would be replaced by database `save()` method which would handle also the id generation
        return new Receipt((int) (Math.random() * 1000000),
                           bonusCardId,
                           receiptItems);
    }

    private static int getNumberOfProductsWithGivenType(Map<Integer, Product> orderedProductsMap,
                                                        Map<Integer, Long> orderedProductsQuantity,
                                                        ProductType productType) {
        return (int) orderedProductsMap.values()
                                       .stream()
                                       .filter(product -> product.type().equals(productType))
                                       .map(product -> orderedProductsQuantity.get(product.id()).intValue())
                                       .count();
    }

    private List<ReceiptItem> mapProductsToReceiptItems(Map<Integer, Product> orderedProductsMap,
                                                        Map<Integer, Long> orderedProductsQuantity) {
        return orderedProductsMap.values()
                                 .stream()
                                 .map(product -> mapProductToReceiptItem(product,
                                                                         orderedProductsQuantity.get(product.id())
                                                                                                .intValue()))
                                 .collect(toList());
    }

    private ReceiptItem mapProductToReceiptItem(Product product, int quantity) {
        return new ReceiptItem(product.name(),
                               product.price(),
                               quantity,
                                 quantity * product.price(),
                               product.currency());
    }

}
