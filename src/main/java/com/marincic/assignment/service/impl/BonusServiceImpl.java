package com.marincic.assignment.service.impl;

import com.marincic.assignment.model.BonusCard;
import com.marincic.assignment.model.Product;
import com.marincic.assignment.model.enumeration.ProductType;
import com.marincic.assignment.repository.BonusCardRepository;
import com.marincic.assignment.service.BonusService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.marincic.assignment.model.enumeration.ProductType.BEVERAGE;
import static com.marincic.assignment.model.enumeration.ProductType.EXTRAS;
import static com.marincic.assignment.model.enumeration.ProductType.SNACK;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;

public class BonusServiceImpl implements BonusService {

    private static final long FREE_BEVERAGE_ELIGIBLE = 5L;

    private final BonusCardRepository bonusCardRepository;

    public BonusServiceImpl(BonusCardRepository bonusCardRepository) {
        this.bonusCardRepository = bonusCardRepository;
    }

    @Override
    public List<Product> getDiscountsFromBonusCard(Integer bonusCardId,
                                                   List<Integer> productIds,
                                                   Map<Integer, Product> orderedProductsMap,
                                                   Map<Integer, Long> orderedProductsQuantity) {
        long beverageCount = getNumberOfProductsWithGivenType(orderedProductsMap, orderedProductsQuantity, BEVERAGE);

        Optional<BonusCard> bonusCardOptional = bonusCardRepository.findById(bonusCardId);
        if (bonusCardOptional.isPresent()) {
            BonusCard bonusCard = bonusCardOptional.get();

            long numberOfFreeBeveragesToBeClaimed = getNumberOfFreeBeveragesToBeClaimed(bonusCard, beverageCount);

            bonusCardRepository.save(new BonusCard(bonusCard.id(),
                                                   bonusCard.numberOfBeveragesPurchased() + beverageCount));

            return productIds.stream()
                             .map(orderedProductsMap::get)
                             .filter(product -> product.type().equals(BEVERAGE))
                             .sorted(comparing(Product::price))
                             .limit(numberOfFreeBeveragesToBeClaimed)
                             .map(BonusServiceImpl::negatePrice)
                             .toList();
        }

        return List.of();
    }

    @Override
    public Optional<Product> getFreeExtra(Integer bonusCardId,
                                List<Integer> productIds,
                                Map<Integer, Product> orderedProductsMap,
                                Map<Integer, Long> orderedProductsQuantity) {
        long beverageCount = getNumberOfProductsWithGivenType(orderedProductsMap, orderedProductsQuantity, BEVERAGE);
        long snacksCount = getNumberOfProductsWithGivenType(orderedProductsMap, orderedProductsQuantity, SNACK);

        if (beverageCount > 0 && snacksCount > 0) {
            return orderedProductsMap.values()
                                     .stream()
                                     .filter(product -> product.type().equals(EXTRAS))
                                     .min(comparing(Product::price))
                                     .map(BonusServiceImpl::negatePrice);
        }

        return empty();
    }

    private static long getNumberOfFreeBeveragesToBeClaimed(BonusCard bonusCard, long beverageCount) {
        long numberOfFreeBeveragesAlreadyClaimed = bonusCard.numberOfBeveragesPurchased() / FREE_BEVERAGE_ELIGIBLE;
        long updatedNumberOfBeveragesPurchased = bonusCard.numberOfBeveragesPurchased() + beverageCount;

        return (updatedNumberOfBeveragesPurchased / FREE_BEVERAGE_ELIGIBLE) - numberOfFreeBeveragesAlreadyClaimed;
    }

    private static long getNumberOfProductsWithGivenType(Map<Integer, Product> orderedProductsMap,
                                                         Map<Integer, Long> orderedProductsQuantity,
                                                         ProductType productType) {
        return orderedProductsMap.values()
                                 .stream()
                                 .filter(product -> product.type().equals(productType))
                                 .map(product -> orderedProductsQuantity.get(product.id()))
                                 .reduce(0L, Long::sum);
    }

    private static Product negatePrice(Product product) {
        return new Product(product.id(),
                           product.name(),
                           product.price() * -1,
                           product.currency(),
                           product.type());
    }

}
