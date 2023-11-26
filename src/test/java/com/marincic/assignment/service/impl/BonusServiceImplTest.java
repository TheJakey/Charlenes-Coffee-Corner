package com.marincic.assignment.service.impl;

import com.marincic.assignment.model.Product;
import com.marincic.assignment.repository.BonusCardRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.marincic.assignment.model.enumeration.ProductType.BEVERAGE;
import static com.marincic.assignment.model.enumeration.ProductType.EXTRAS;
import static com.marincic.assignment.model.enumeration.ProductType.SNACK;
import static java.util.Currency.getInstance;
import static java.util.Locale.forLanguageTag;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BonusServiceImplTest {

    private final BonusServiceImpl bonusService = new BonusServiceImpl(new BonusCardRepository());

    @Test
    public void whenBonusCardIdIsInvalidThenReturnEmptyArray() {
        assertEquals(List.of(), bonusService.getDiscountsFromBonusCard(0, List.of(), Map.of(), Map.of()));
    }

    @Test
    public void whenBonusCardIdIsValidThenReturnDiscounts() {
        List<Integer> productIds = List.of(1, 1, 1, 1, 1);
        Map<Integer, Long> orderedProductsQuantity = productIds.stream()
                                                               .collect(groupingBy(identity(), counting()));
        Map<Integer, Product> orderedProductsMap = createBeverageMap();

        List<Product> result = bonusService.getDiscountsFromBonusCard(901,
                                                                      productIds,
                                                                      orderedProductsMap,
                                                                      orderedProductsQuantity);

        assertEquals(1, result.size());
        assertEquals(negatePrice(orderedProductsMap.get(1)), result.get(0));
    }

    @Test
    public void whenBonusCardIdIsValidAndTenBeveragesAreOrderedThenReturnTwoDiscounts() {
        List<Integer> productIds = List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        Map<Integer, Long> orderedProductsQuantity = productIds.stream()
                                                               .collect(groupingBy(identity(), counting()));
        Map<Integer, Product> orderedProductsMap = createBeverageMap();

        List<Product> result = bonusService.getDiscountsFromBonusCard(901,
                                                                      productIds,
                                                                      orderedProductsMap,
                                                                      orderedProductsQuantity);

        assertEquals(2, result.size());
        assertEquals(negatePrice(orderedProductsMap.get(1)), result.get(0));
        assertEquals(negatePrice(orderedProductsMap.get(1)), result.get(1));
    }

    @Test
    public void whenBonusCardIdIsValidAndDifferentBeveragesAreOrderedThenReturnDiscountOnCheaperOne() {
        List<Integer> productIds = List.of(1, 2, 1);
        Map<Integer, Long> orderedProductsQuantity = productIds.stream()
                                                               .collect(groupingBy(identity(), counting()));
        Map<Integer, Product> orderedProductsMap = createBiggerBeverageMap();

        List<Product> result = bonusService.getDiscountsFromBonusCard(901,
                                                                      productIds,
                                                                      orderedProductsMap,
                                                                      orderedProductsQuantity);

        assertEquals(1, result.size());
        assertEquals(negatePrice(orderedProductsMap.get(1)), result.get(0));
    }

    @Test
    public void whenOnlyBeveragesWereBoughtThenReturnNoFreeExtra() {
        List<Integer> productIds = List.of(1, 2, 1);
        Map<Integer, Long> orderedProductsQuantity = productIds.stream()
                                                               .collect(groupingBy(identity(), counting()));
        Map<Integer, Product> orderedProductsMap = createBiggerBeverageMap();

        Optional<Product> freeExtra = bonusService.getFreeExtra(null,
                                                                productIds,
                                                                orderedProductsMap,
                                                                orderedProductsQuantity);

        assertFalse(freeExtra.isPresent());
    }

    @Test
    public void whenOnlySnacksWereBoughtThenReturnNoFreeExtra() {
        List<Integer> productIds = List.of(3);
        Map<Integer, Long> orderedProductsQuantity = productIds.stream()
                                                               .collect(groupingBy(identity(), counting()));
        Map<Integer, Product> orderedProductsMap = createSnackMap();

        Optional<Product> freeExtra = bonusService.getFreeExtra(null,
                                                                productIds,
                                                                orderedProductsMap,
                                                                orderedProductsQuantity);

        assertFalse(freeExtra.isPresent());
    }

    @Test
    public void whenSnackAndBeverageWasBoughtThenReturnNoFreeExtra() {
        List<Integer> productIds = List.of(1, 3);
        Map<Integer, Long> orderedProductsQuantity = productIds.stream()
                                                               .collect(groupingBy(identity(), counting()));
        Map<Integer, Product> orderedProductsMap = new HashMap<>(createSnackMap());
        orderedProductsMap.putAll(createBeverageMap());

        Optional<Product> freeExtra = bonusService.getFreeExtra(null,
                                                                productIds,
                                                                orderedProductsMap,
                                                                orderedProductsQuantity);

        assertFalse(freeExtra.isPresent());
    }

    @Test
    public void whenSnackAndBeverageAndTwoExtrasWereBoughtThenReturnTheCheaperOneAsFreeExtra() {
        List<Integer> productIds = List.of(1, 1, 3, 4, 5);
        Map<Integer, Long> orderedProductsQuantity = productIds.stream()
                                                               .collect(groupingBy(identity(), counting()));
        Map<Integer, Product> orderedProductsMap = new HashMap<>(createSnackMap());
        orderedProductsMap.putAll(createBeverageMap());
        orderedProductsMap.putAll(createBiggerExtrasMap());

        Optional<Product> freeExtra = bonusService.getFreeExtra(null,
                                                                productIds,
                                                                orderedProductsMap,
                                                                orderedProductsQuantity);

        assertTrue(freeExtra.isPresent());
        assertEquals(negatePrice(orderedProductsMap.get(5)), freeExtra.get());
        assertEquals(-0.3, freeExtra.get().price());
    }

    private static Map<Integer, Product> createBeverageMap() {
        return Map.of(1,
                      new Product(1, "Coffee", 1.0, getInstance(forLanguageTag("de-CH")), BEVERAGE));
    }

    private static Map<Integer, Product> createBiggerBeverageMap() {
        return Map.of(1,
                      new Product(1, "Coffee", 1.0, getInstance(forLanguageTag("de-CH")), BEVERAGE),
                      2,
                      new Product(2, "Orange juice", 2.0, getInstance(forLanguageTag("de-CH")), BEVERAGE));
    }

    private static Map<Integer, Product> createSnackMap() {
        return Map.of(3,
                      new Product(3, "Bacon Roll", 1.0, getInstance(forLanguageTag("de-CH")), SNACK));
    }

    private static Map<Integer, Product> createBiggerExtrasMap() {
        return Map.of(4,
                      new Product(4, "Extra", 0.5, getInstance(forLanguageTag("de-CH")), EXTRAS),
                      5,
                      new Product(5, "Extra", 0.3, getInstance(forLanguageTag("de-CH")), EXTRAS));
    }

    private static Product negatePrice(Product product) {
        return new Product(product.id(),
                           product.name(),
                           product.price() * -1,
                           product.currency(),
                           product.type());
    }

}
