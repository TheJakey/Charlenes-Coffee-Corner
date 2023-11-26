package com.marincic.assignment;

import com.marincic.assignment.model.Receipt;
import com.marincic.assignment.model.ReceiptItem;
import com.marincic.assignment.model.enumeration.CashRegisterItems;
import com.marincic.assignment.repository.BonusCardRepository;
import com.marincic.assignment.repository.ProductRepository;
import com.marincic.assignment.service.ReceiptService;
import com.marincic.assignment.service.impl.ReceiptServiceImpl;

import java.util.Currency;
import java.util.List;

public class App {

    private static final ReceiptService receiptService = new ReceiptServiceImpl(new ProductRepository(),
                                                                                new BonusCardRepository());

    public static void main(String[] args) {
        System.out.println("Welcome to Coffee Corner!n\n\n");

        List<CashRegisterItems> orderedItems = List.of(
                CashRegisterItems.BACON_ROLL,
                CashRegisterItems.COFFEE_SMALL,
                CashRegisterItems.COFFEE_LARGE,
                CashRegisterItems.COFFEE_LARGE,
                CashRegisterItems.EXTRA_MILK,
                CashRegisterItems.SPECIAL_ROAST_COFFEE
        );
        Integer bonusCardId = 902;

        Receipt receipt = receiptService.createReceipt(bonusCardId,
                                                       orderedItems.stream()
                                                                   .map(CashRegisterItems::getId)
                                                                   .toList());

        printReceipt(receipt);
    }

    private static void printReceipt(Receipt receipt) {
        System.out.println("Welcome to Coffee Corner! Here is your receipt:");
        System.out.println("==========================================================");
        System.out.println("Receipt ID: " + receipt.id());
        System.out.println("Bonus Card ID: " + receipt.bonusCardId());
        System.out.println("==========================================================");
        System.out.printf("%-21s %-10s %-10s %-10s\n", "Item", "Quantity", "Price", "Total");
        System.out.println("==========================================================");

        for (ReceiptItem item : receipt.receiptItems()) {
            System.out.printf("%-21s %-10d %-10.2f %-10.2f %s\n",
                              item.name(),
                              item.quantity(),
                              item.price(),
                              item.totalPrice(),
                              item.currency().getSymbol());
        }

        System.out.printf("\n%-21s %-10s %-10s %-10.2f %s\n",
                          "Total",
                          "",
                          "",
                          receipt.receiptItems().stream().map(ReceiptItem::totalPrice).reduce(0.0, Double::sum),
                          receipt.receiptItems()
                                 .stream()
                                 .findFirst()
                                 .map(ReceiptItem::currency)
                                 .map(Currency::getSymbol)
                                 .orElse(""));
        System.out.println("==========================================================");
        System.out.println("Thank you for your visit! See you soon!");
        System.out.println("==========================================================");
        System.out.println("Advertisement Section:");
        System.out.println(" Skilled developer looking for a job!");
        System.out.println(" Will code for coffee&pizza!");
        System.out.println(" Years of experience, great attitude and a lot of passion!");
        System.out.println(" For contact, please reach out to Igor");
        System.out.println("==========================================================\n\n\n");

        // generate qr code that says "Jakub is really cool guy, you should hire him!" and print it
    }
}
