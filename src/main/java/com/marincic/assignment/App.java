package com.marincic.assignment;

import com.marincic.assignment.model.Receipt;
import com.marincic.assignment.model.ReceiptItem;
import com.marincic.assignment.model.enumeration.CashRegisterItem;
import com.marincic.assignment.repository.BonusCardRepository;
import com.marincic.assignment.repository.ProductRepository;
import com.marincic.assignment.service.ReceiptService;
import com.marincic.assignment.service.impl.ReceiptServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Scanner;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class App {

    private static final Scanner scanner = new Scanner(System.in);

    private static final ReceiptService receiptService = new ReceiptServiceImpl(new ProductRepository(),
                                                                                new BonusCardRepository());

    public static void main(String[] args) {
        List<CashRegisterItem> orderedItems = new ArrayList<>();

        System.out.println("Welcome to Coffee Corner!\n\n");
        displayProductMenu();

        while (true) {
            System.out.print("Enter product ID (or '0' to finish): ");
            int productId = scanner.nextInt();

            if (productId == 0) {
                break;
            }

            CashRegisterItem cashRegisterItem = getCashRegisterItem(productId);
            if (isNull(cashRegisterItem)) {
                System.out.println("Invalid product ID. Please try again.");
                continue;
            }

            orderedItems.add(cashRegisterItem);
        }

        if (orderedItems.isEmpty()) {
            System.out.println("No products ordered. Exiting...");
            return;
        }

        System.out.print("Enter Bonus Card ID (or '0' to finish): ");
        int bonusCardIdInput = scanner.nextInt();
        Integer bonusCardId = bonusCardIdInput != 0 ? bonusCardIdInput : null;

        Receipt receipt = receiptService.createReceipt(bonusCardId,
                                                       orderedItems.stream()
                                                                   .map(CashRegisterItem::getId)
                                                                   .toList());

        printReceipt(receipt);
    }

    private static void displayProductMenu() {
        System.out.println("Available Products:");
        Arrays.stream(CashRegisterItem.values()) // replace with DB call
              .map(item -> item.getId() + ". " + item.getName())
              .forEach(System.out::println);
        System.out.println();
    }

    private static CashRegisterItem getCashRegisterItem(int productId) {
        return Arrays.stream(CashRegisterItem.values())
                     .filter(item -> item.getId() == productId)
                     .findFirst()
                     .orElse(null);
    }

    private static void printReceipt(Receipt receipt) {
        System.out.println("\n\nHere is your receipt:");
        System.out.println("==========================================================");
        System.out.println("Receipt ID: " + receipt.id());
        System.out.println("Bonus Card ID: " + (nonNull(receipt.bonusCardId()) ? receipt.bonusCardId() : " - "));
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
    }
}
