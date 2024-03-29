package com.seinksansdoozebank.fr.model.cards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    private Card monasteryCard;
    private Card monasteryCardBis;
    private Card barrackCard;

    @BeforeEach
    void setUp() {
        monasteryCard = new Card(District.MONASTERY);
        barrackCard = new Card(District.BARRACK);
        monasteryCardBis = new Card(District.MONASTERY);
    }

    @Test
    void equalsWithSameCard() {
        assertEquals(monasteryCard, monasteryCard);
    }

    @Test
    void equalsWithSameDistrictButDifferentCard() {
        assertNotEquals(monasteryCard, monasteryCardBis);
    }

    @Test
    void equalsWithDifferentCard() {
        assertNotEquals(monasteryCard, barrackCard);
    }

    @Test
    void equalsTestOnNull() {
        District district = District.TEMPLE;
        assertNotEquals(monasteryCard, district);
    }
}