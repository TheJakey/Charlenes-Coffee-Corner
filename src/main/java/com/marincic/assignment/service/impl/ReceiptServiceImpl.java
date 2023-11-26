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
import java.util.Optional;

import static com.marincic.assignment.model.enumeration.ProductType.BEVERAGE;
import static com.marincic.assignment.model.enumeration.ProductType.EXTRAS;
import static com.marincic.assignment.model.enumeration.ProductType.SNACK;
import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ReceiptServiceImpl implements ReceiptService {

    private final ProductRepository productRepository;
    private final BonusCardRepository bonusCardRepository;

    public ReceiptServiceImpl(ProductRepository productRepository,
                              BonusCardRepository bonusCardRepository) {
        this.productRepository = productRepository;
        this.bonusCardRepository = bonusCardRepository;
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

        int beverageCount = getNumberOfProductsWithGivenType(orderedProductsMap, orderedProductsQuantity, BEVERAGE);

        Optional<BonusCard> bonusCardOptional = bonusCardRepository.findById(bonusCardId);
        if (bonusCardOptional.isPresent()) {
            BonusCard bonusCard = bonusCardOptional.get();

            int numberOfFreeBeveragesAlreadyClaimed = bonusCard.numberOfBeveragesPurchased() / 5;

            BonusCard newBonusCardState = new BonusCard(bonusCard.id(),
                                                        bonusCard.numberOfBeveragesPurchased() + beverageCount);

            int numberOfFreeBeveragesToBeClaimed = newBonusCardState.numberOfBeveragesPurchased() / 5 - numberOfFreeBeveragesAlreadyClaimed;

            productIds.stream()
                      .map(orderedProductsMap::get)
                      .filter(product -> product.type().equals(BEVERAGE))
                      .sorted(comparing(Product::price))
                      .limit(numberOfFreeBeveragesToBeClaimed)
                      .map(ReceiptServiceImpl::negatePrice)
                      .map(product -> mapProductToReceiptItem(product, 1))
                      .forEach(receiptItems::add);

            // save newBonusCardState to DB
        }

        int snacksCount = getNumberOfProductsWithGivenType(orderedProductsMap, orderedProductsQuantity, SNACK);
        if (beverageCount > 0 && snacksCount > 0) {
            orderedProductsMap.values()
                              .stream()
                              .filter(product -> product.type().equals(EXTRAS))
                              .min(comparing(Product::price))
                              .map(ReceiptServiceImpl::negatePrice)
                              .map(product -> mapProductToReceiptItem(product, 1))
                              .ifPresent(receiptItems::add);
        }

        // This would be replaced by database `save()` method which would handle also the id generation
        return new Receipt(randomUUID(), bonusCardId, receiptItems);
    }

    private static Product negatePrice(Product product) {
        return new Product(product.id(),
                           product.name(),
                           product.price() * -1,
                           product.currency(),
                           product.type());
    }

    private static int getNumberOfProductsWithGivenType(Map<Integer, Product> orderedProductsMap,
                                                        Map<Integer, Long> orderedProductsQuantity,
                                                        ProductType productType) {
        return orderedProductsMap.values()
                                 .stream()
                                 .filter(product -> product.type().equals(productType))
                                 .map(product -> orderedProductsQuantity.get(product.id()).intValue())
                                 .reduce(0, Integer::sum);
    }

    private List<ReceiptItem> mapProductsToReceiptItems(Map<Integer, Product> orderedProductsMap,
                                                        Map<Integer, Long> orderedProductsQuantity) {
        return orderedProductsMap.values()
                                 .stream()
                                 .map(product -> mapProductToReceiptItem(product,
                                                                         orderedProductsQuantity.get(product.id())
                                                                                                .intValue())) // TODO: int to long
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
