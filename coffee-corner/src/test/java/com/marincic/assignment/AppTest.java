package com.marincic.assignment;

import com.marincic.assignment.model.Receipt;
import com.marincic.assignment.model.ReceiptItem;
import com.marincic.assignment.repository.BonusCardRepository;
import com.marincic.assignment.repository.ProductRepository;
import com.marincic.assignment.service.ReceiptService;
import com.marincic.assignment.service.impl.ReceiptServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.marincic.assignment.model.enumeration.CashRegisterItems.BACON_ROLL;
import static com.marincic.assignment.model.enumeration.CashRegisterItems.COFFEE_LARGE;
import static com.marincic.assignment.model.enumeration.CashRegisterItems.COFFEE_MEDIUM;
import static com.marincic.assignment.model.enumeration.CashRegisterItems.EXTRA_MILK;
import static com.marincic.assignment.model.enumeration.CashRegisterItems.FRESHLY_SQUEEZED_ORANGE_JUICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AppTest {

    private final ReceiptService receiptService = new ReceiptServiceImpl(new ProductRepository(),
                                                                         new BonusCardRepository());

    @Test
    public void whenOrderOneLargeCoffeeThenReturnCorrectReceipt () {
        int id = COFFEE_LARGE.getId();

        Receipt receipt = receiptService.createReceipt(null, List.of(id));

        assertEquals(1, receipt.receiptItems().size());

        assertEquals(COFFEE_LARGE.getName(), receipt.receiptItems().get(0).name());
        assertEquals(1, receipt.receiptItems().get(0).quantity());
        assertEquals(3.5, receipt.receiptItems().get(0).price());
        assertEquals(3.5, receipt.receiptItems().get(0).totalPrice());

        assertEquals(3.5, countTotal(receipt.receiptItems()));
        assertNull(receipt.bonusCardId());
    }

    @Test
    public void whenOrderTenMediumCoffeeThenReturnCorrectReceipt () {
        int id = COFFEE_MEDIUM.getId();

        Receipt receipt = receiptService.createReceipt(null,
                                                       List.of(id, id, id, id, id, id, id, id, id, id));

        assertEquals(1, receipt.receiptItems().size());

        assertEquals(COFFEE_MEDIUM.getName(), receipt.receiptItems().get(0).name());
        assertEquals(10, receipt.receiptItems().get(0).quantity());
        assertEquals(3.0, receipt.receiptItems().get(0).price());
        assertEquals(30.0, receipt.receiptItems().get(0).totalPrice());

        assertEquals(30.0, countTotal(receipt.receiptItems()));
        assertNull(receipt.bonusCardId());
    }

    @Test
    public void whenOrderBeverageAndSnackAndMilkThenReturnCorrectReceiptWithFreeMilk() {
        int beverageId = COFFEE_MEDIUM.getId();
        int snackId = BACON_ROLL.getId();
        int milkId = EXTRA_MILK.getId();

        Receipt receipt = receiptService.createReceipt(null,
                                                       List.of(beverageId, snackId, milkId));

        assertEquals(4, receipt.receiptItems().size());

        assertEquals(COFFEE_MEDIUM.getName(), receipt.receiptItems().get(0).name());
        assertEquals(1, receipt.receiptItems().get(0).quantity());
        assertEquals(3.0, receipt.receiptItems().get(0).price());
        assertEquals(3.0, receipt.receiptItems().get(0).totalPrice());

        assertEquals(BACON_ROLL.getName(), receipt.receiptItems().get(1).name());
        assertEquals(1, receipt.receiptItems().get(1).quantity());
        assertEquals(4.5, receipt.receiptItems().get(1).price());
        assertEquals(4.5, receipt.receiptItems().get(1).totalPrice());

        assertEquals(EXTRA_MILK.getName(), receipt.receiptItems().get(2).name());
        assertEquals(1, receipt.receiptItems().get(2).quantity());
        assertEquals(0.3, receipt.receiptItems().get(2).price());
        assertEquals(0.3, receipt.receiptItems().get(2).totalPrice());

        assertEquals(EXTRA_MILK.getName(), receipt.receiptItems().get(2).name());
        assertEquals(1, receipt.receiptItems().get(3).quantity());
        assertEquals(-0.3, receipt.receiptItems().get(3).price());
        assertEquals(-0.3, receipt.receiptItems().get(3).totalPrice());

        assertEquals(7.5, countTotal(receipt.receiptItems()));
        assertNull(receipt.bonusCardId());
    }

    @Test
    public void whenFifthBeverageOnBonusCardIsBoughtThenCorrectReceiptWithDiscount() {
        int beverageId = FRESHLY_SQUEEZED_ORANGE_JUICE.getId();
        int bonusCardId = 902;

        Receipt receipt = receiptService.createReceipt(bonusCardId,
                                                       List.of(beverageId));

        assertEquals(2, receipt.receiptItems().size());

        assertEquals(FRESHLY_SQUEEZED_ORANGE_JUICE.getName(), receipt.receiptItems().get(0).name());
        assertEquals(1, receipt.receiptItems().get(0).quantity());
        assertEquals(3.95, receipt.receiptItems().get(0).price());
        assertEquals(3.95, receipt.receiptItems().get(0).totalPrice());

        assertEquals(FRESHLY_SQUEEZED_ORANGE_JUICE.getName(), receipt.receiptItems().get(1).name());
        assertEquals(1, receipt.receiptItems().get(1).quantity());
        assertEquals(-3.95, receipt.receiptItems().get(1).price());
        assertEquals(-3.95, receipt.receiptItems().get(1).totalPrice());

        assertEquals(0.0, countTotal(receipt.receiptItems()));
        assertEquals(bonusCardId, receipt.bonusCardId());
    }

    @Test
    public void whenElevenBeveragesBoughtAndBonusCardHasOneThenCorrectReceiptWithTwoDiscountsOfTheCheapestBeverages() {
        int id1 = FRESHLY_SQUEEZED_ORANGE_JUICE.getId();
        int id2 = COFFEE_LARGE.getId();
        int bonusCardId = 903;

        Receipt receipt = receiptService.createReceipt(bonusCardId,
                                                       List.of(id1, id1, id1, id1, id1, id1, id1, id1, id1, id1, id2));

        assertEquals(4, receipt.receiptItems().size());

        assertEquals(COFFEE_LARGE.getName(), receipt.receiptItems().get(0).name());
        assertEquals(1, receipt.receiptItems().get(0).quantity());
        assertEquals(3.5, receipt.receiptItems().get(0).price());
        assertEquals(3.5, receipt.receiptItems().get(0).totalPrice());

        assertEquals(FRESHLY_SQUEEZED_ORANGE_JUICE.getName(), receipt.receiptItems().get(1).name());
        assertEquals(10, receipt.receiptItems().get(1).quantity());
        assertEquals(3.95, receipt.receiptItems().get(1).price());
        assertEquals(39.5, receipt.receiptItems().get(1).totalPrice());

        assertEquals(COFFEE_LARGE.getName(), receipt.receiptItems().get(2).name());
        assertEquals(1, receipt.receiptItems().get(2).quantity());
        assertEquals(-3.5, receipt.receiptItems().get(2).price());
        assertEquals(-3.5, receipt.receiptItems().get(2).totalPrice());

        assertEquals(FRESHLY_SQUEEZED_ORANGE_JUICE.getName(), receipt.receiptItems().get(3).name());
        assertEquals(1, receipt.receiptItems().get(3).quantity());
        assertEquals(-3.95, receipt.receiptItems().get(3).price());
        assertEquals(-3.95, receipt.receiptItems().get(3).totalPrice());

        assertEquals(35.55, countTotal(receipt.receiptItems()));
        assertEquals(bonusCardId, receipt.bonusCardId());
    }

    @Test
    public void whenElevenBeveragesBoughtAndBonusCardHasFourThenCorrectReceiptWithThreeDiscountsOfTheCheapestBeverages() {
        int id1 = FRESHLY_SQUEEZED_ORANGE_JUICE.getId();
        int id2 = COFFEE_LARGE.getId();
        int bonusCardId = 902;

        Receipt receipt = receiptService.createReceipt(bonusCardId,
                                                       List.of(id1, id1, id1, id1, id1, id1, id1, id1, id1, id1, id2));

        assertEquals(5, receipt.receiptItems().size());

        assertEquals(COFFEE_LARGE.getName(), receipt.receiptItems().get(0).name());
        assertEquals(1, receipt.receiptItems().get(0).quantity());
        assertEquals(3.5, receipt.receiptItems().get(0).price());
        assertEquals(3.5, receipt.receiptItems().get(0).totalPrice());

        assertEquals(FRESHLY_SQUEEZED_ORANGE_JUICE.getName(), receipt.receiptItems().get(1).name());
        assertEquals(10, receipt.receiptItems().get(1).quantity());
        assertEquals(3.95, receipt.receiptItems().get(1).price());
        assertEquals(39.5, receipt.receiptItems().get(1).totalPrice());

        assertEquals(COFFEE_LARGE.getName(), receipt.receiptItems().get(2).name());
        assertEquals(1, receipt.receiptItems().get(2).quantity());
        assertEquals(-3.5, receipt.receiptItems().get(2).price());
        assertEquals(-3.5, receipt.receiptItems().get(2).totalPrice());

        assertEquals(FRESHLY_SQUEEZED_ORANGE_JUICE.getName(), receipt.receiptItems().get(3).name());
        assertEquals(1, receipt.receiptItems().get(3).quantity());
        assertEquals(-3.95, receipt.receiptItems().get(3).price());
        assertEquals(-3.95, receipt.receiptItems().get(3).totalPrice());

        assertEquals(FRESHLY_SQUEEZED_ORANGE_JUICE.getName(), receipt.receiptItems().get(3).name());
        assertEquals(1, receipt.receiptItems().get(3).quantity());
        assertEquals(-3.95, receipt.receiptItems().get(3).price());
        assertEquals(-3.95, receipt.receiptItems().get(3).totalPrice());

        assertEquals(31.6, countTotal(receipt.receiptItems()));
        assertEquals(bonusCardId, receipt.bonusCardId());
    }

    @Test
    public void whenUnknownProductRequestedThenFail() {
        assertThrows(IllegalArgumentException.class,
                     () -> receiptService.createReceipt(null, List.of(69)));
    }

    @Test
    public void whenZeroProductRequestedThenFail() {
        assertThrows(IllegalArgumentException.class,
                     () -> receiptService.createReceipt(null, List.of()));
    }

    private double countTotal(List<ReceiptItem> receiptItems) {
        return receiptItems.stream()
                           .mapToDouble(ReceiptItem::totalPrice)
                           .sum();
    }

}
