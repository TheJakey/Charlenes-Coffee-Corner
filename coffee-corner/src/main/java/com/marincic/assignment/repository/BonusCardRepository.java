package com.marincic.assignment.repository;


import com.marincic.assignment.model.BonusCard;

import java.util.List;

public class BonusCardRepository {

    public BonusCard findById(Integer bonusCardId) {
        return mockedBonusCards().stream()
                                    .filter(bonusCard -> bonusCard.id().equals(bonusCardId))
                                    .findFirst()
                                    .orElse(null);
    }

    private static List<BonusCard> mockedBonusCards() {
        return List.of(
                new BonusCard(901, 2),
                new BonusCard(902, 4),
                new BonusCard(903, 1)
        );
    }

}