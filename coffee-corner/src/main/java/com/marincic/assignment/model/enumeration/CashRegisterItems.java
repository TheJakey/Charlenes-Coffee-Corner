package com.marincic.assignment.model.enumeration;

public enum CashRegisterItems {

    COFFEE_SMALL(1,"Coffee (small)"),
    COFFEE_MEDIUM(2, "Coffee (medium)"),
    COFFEE_LARGE(3, "Coffee (large)"),
    BACON_ROLL(4, "Bacon Roll"),
    FRESHLY_SQUEEZED_ORANGE_JUICE(5, "Freshly squeezed orange juice (0.25l)"),
    EXTRA_MILK(6, "Extra milk"),
    FOAMED_MILK(7, "Foamed milk"),
    SPECIAL_ROAST_COFFEE(8, "Special roast coffee");

    private final int id;
    private final String name;

    CashRegisterItems(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
