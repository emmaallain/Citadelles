package com.seinksansdoozebank.fr.model.player;

import com.seinksansdoozebank.fr.model.bank.Bank;
import com.seinksansdoozebank.fr.model.cards.Card;
import com.seinksansdoozebank.fr.model.cards.Deck;
import com.seinksansdoozebank.fr.model.cards.District;
import com.seinksansdoozebank.fr.model.cards.DistrictType;
import com.seinksansdoozebank.fr.model.character.abstracts.Character;
import com.seinksansdoozebank.fr.model.character.commoncharacters.Bishop;
import com.seinksansdoozebank.fr.model.character.commoncharacters.Warlord;
import com.seinksansdoozebank.fr.model.character.commoncharacters.King;
import com.seinksansdoozebank.fr.model.character.commoncharacters.Merchant;
import com.seinksansdoozebank.fr.model.character.specialscharacters.Architect;
import com.seinksansdoozebank.fr.model.character.specialscharacters.Assassin;
import com.seinksansdoozebank.fr.model.character.specialscharacters.Thief;
import com.seinksansdoozebank.fr.view.Cli;
import com.seinksansdoozebank.fr.view.IView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RandomBotTest {
    RandomBot spyRandomBot;
    IView view;
    Deck deck;
    Bank bank;
    Card cardCostThree;
    Card cardCostFive;

    @BeforeEach
    void setup() {
        view = mock(Cli.class);
        deck = spy(new Deck());
        bank = mock(Bank.class);
        cardCostThree = new Card(District.DONJON);
        cardCostFive = new Card(District.FORTRESS);
        spyRandomBot = spy(new RandomBot(10, deck, view, bank));
    }

    @Test
    void play() {
        Optional<Card> optDistrict = Optional.of(cardCostThree);
        doReturn(optDistrict).when(spyRandomBot).playACard();
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(new Bishop())));
        spyRandomBot.play();

        verify(spyRandomBot, times(1)).pickSomething();
        verify(spyRandomBot, atMostOnce()).playACard();
        verify(view, times(1)).displayPlayerStartPlaying(spyRandomBot);
        verify(view, times(1)).displayPlayerRevealCharacter(spyRandomBot);
        verify(view, times(2)).displayPlayerInfo(spyRandomBot);
        verify(view, atMostOnce()).displayPlayerPlaysCard(any(), any());
    }

    @Test
    void playWhereCharacterIsDead() {
        King king = new King();
        when(spyRandomBot.getCharacter()).thenReturn(king);
        king.kill();
        assertThrows(IllegalStateException.class, () -> spyRandomBot.play());
    }

    @Test
    void playWithArchitect() {
        Optional<Card> optDistrict = Optional.of(cardCostThree);
        doReturn(optDistrict).when(spyRandomBot).playACard();
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(new Architect())));
        spyRandomBot.play();

        verify(spyRandomBot, times(1)).pickSomething();
        verify(spyRandomBot, atMost(3)).playACard();
        verify(view, times(1)).displayPlayerStartPlaying(spyRandomBot);
        verify(view, times(1)).displayPlayerRevealCharacter(spyRandomBot);
        verify(view, times(2)).displayPlayerInfo(spyRandomBot);
        verify(view, atMost(3)).displayPlayerPlaysCard(any(), any());
    }

    @Test
    void playWithAssassinWithOneGoodCharacterToKill() {
        Optional<Card> optDistrict = Optional.of(cardCostThree);
        doReturn(optDistrict).when(spyRandomBot).playACard();
        Assassin assassin = spy(new Assassin());
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(assassin)));
        List<Opponent> opponents = new ArrayList<>();
        RandomBot opponent = new RandomBot(10, deck, view, bank);
        opponent.chooseCharacter(new ArrayList<>(List.of(new Warlord())));
        opponents.add(opponent);
        when(spyRandomBot.getOpponents()).thenReturn(opponents);
        when(spyRandomBot.getAvailableCharacters()).thenReturn(List.of(new Warlord()));

        spyRandomBot.play();

        verify(spyRandomBot, times(1)).pickSomething();
        verify(spyRandomBot, atMost(3)).playACard();
        verify(view, times(1)).displayPlayerStartPlaying(spyRandomBot);
        verify(view, times(1)).displayPlayerRevealCharacter(spyRandomBot);
        verify(view, times(2)).displayPlayerInfo(spyRandomBot);
        verify(view, atMost(1)).displayPlayerPlaysCard(any(), any());
        verify(assassin, times(1)).useEffect(opponent.getCharacter());
    }


    @Test
    void pickSomething() {
        spyRandomBot.pickSomething();
        verify(spyRandomBot, atMostOnce()).pickGold();
        verify(spyRandomBot, atMostOnce()).pickCardsKeepSomeAndDiscardOthers();
    }

    @Test
    void pickTwoDistrictKeepOneDiscardOne() {
        int handSizeBeforePicking = spyRandomBot.getHand().size();
        spyRandomBot.pickCardsKeepSomeAndDiscardOthers();

        verify(view, times(1)).displayPlayerPickCards(spyRandomBot, 1);

        verify(deck, times(2)).pick();
        assertEquals(handSizeBeforePicking + 1, spyRandomBot.getHand().size());
        verify(deck, times(1)).discard(any(Card.class));
    }

    @Test
    void chooseDistrictWithEmptyHand() {
        boolean handIsEmpty = spyRandomBot.getHand().isEmpty();
        Optional<Card> chosenDistrict = spyRandomBot.chooseCard();
        assertTrue(chosenDistrict.isEmpty());
        assertTrue(handIsEmpty);
    }

    @Test
    void chooseDistrictWithNonEmptyHandButNoDistrictToBuildShouldReturnEmptyOptional() {
        spyRandomBot.getHand().add(cardCostThree);
        spyRandomBot.getHand().add(cardCostFive);
        doReturn(false).when(spyRandomBot).canPlayCard(any(Card.class));

        Optional<Card> chosenDistrict = spyRandomBot.chooseCard();

        assertTrue(chosenDistrict.isEmpty());
    }

    @Test
    void chooseDistrictWithNonEmptyHandAndCanBuildDistrictTrueShouldReturnADistrictOfFromTheHand() {
        spyRandomBot.getHand().add(cardCostThree);
        spyRandomBot.getHand().add(cardCostFive);
        doReturn(true).when(spyRandomBot).canPlayCard(any(Card.class));

        Optional<Card> chosenDistrict = spyRandomBot.chooseCard();

        assertFalse(spyRandomBot.getHand().isEmpty());
        assertTrue(chosenDistrict.isPresent());
        assertTrue(spyRandomBot.getHand().contains(chosenDistrict.get()));
    }


    @Test
    void chooseCharacterSetLinkBetweenCharacterAndPlayer() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Bishop());
        characters.add(new King());
        characters.add(new Merchant());
        characters.add(new Warlord());

        spyRandomBot.chooseCharacter(characters);

        Character character = spyRandomBot.getCharacter();
        assertEquals(character.getPlayer().getId(), spyRandomBot.getId());
        verify(view, times(1)).displayPlayerChooseCharacter(spyRandomBot);

    }

    @Test
    void testRandomBotUseEffectWarlord() {
        // Create a mock Random object that always returns true
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(true);

        // Set the mockRandom in the RandomBot for testing
        spyRandomBot.setRandom(mockRandom);
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(new Warlord())));
        List<Card> opponentCitadel = new ArrayList<>(List.of(new Card(District.MARKET_PLACE)));
        Player opponent = new SmartBot(10, deck, view, bank);
        opponent.setCitadel(opponentCitadel);
        opponent.chooseCharacter(new ArrayList<>(List.of(new Merchant())));
        opponent.reveal();
        spyRandomBot.setOpponents(new ArrayList<>(List.of(opponent)));
        // Test the useEffect method
        spyRandomBot.getCharacter().applyEffect();
        verify(spyRandomBot, times(1)).chooseWarlordTarget(any());
    }

    @Test
    void testRandomBotCantUseEffectWarlord() {
        // Create a mock Random object that always returns true
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(true);

        // Set the mockRandom in the RandomBot for testing
        spyRandomBot.setRandom(mockRandom);
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(new Warlord())));
        List<Card> opponentCitadel = new ArrayList<>(List.of(new Card(District.MARKET_PLACE)));
        Player opponent = new SmartBot(10, deck, view, bank);
        opponent.setCitadel(opponentCitadel);
        opponent.chooseCharacter(new ArrayList<>(List.of(new Bishop())));
        opponent.reveal();
        spyRandomBot.setOpponents(new ArrayList<>(List.of(opponent)));
        // Test the useEffect method
        int nbGold = spyRandomBot.getNbGold();
        spyRandomBot.getCharacter().applyEffect();
        verify(spyRandomBot, times(0)).chooseWarlordTarget(any());
        assertEquals(nbGold, spyRandomBot.getNbGold());
    }

    @Test
    void testWantToUseManufactureEffect() {
        // Create a mock Random object that always returns true
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(true);

        // Set the mockRandom in the RandomBot for testing
        spyRandomBot.setRandom(mockRandom);

        // Test the wantToUseEffect method
        assertTrue(spyRandomBot.wantToUseManufactureEffect());
    }

    /**
     * On vérifie que le bot garde une carte aléatoirement dans tous les cas
     */
    @Test
    void keepOneDiscardOthersTest() {
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(false);
        List<Card> cardPicked = new ArrayList<>(List.of(new Card(District.MANOR), new Card(District.TAVERN), new Card(District.PORT)));

        assertNotNull(spyRandomBot.keepOneDiscardOthers(cardPicked));
    }

    /**
     * On vérifie que le randomBot qui est un voleur utilise son effet sur son opposant.
     */
    @Test
    void randomBotUseEffectOfTheThiefTest() {
        Player player = spy(new RandomBot(2, deck, view, bank));
        Thief thief = spy(new Thief());
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(thief)));

        Bishop bishop = spy(new Bishop());
        player.chooseCharacter(new ArrayList<>(List.of(bishop)));
        when(spyRandomBot.getAvailableCharacters()).thenReturn(List.of(bishop));

        spyRandomBot.getCharacter().applyEffect();

        verify(view, times(1)).displayPlayerUseThiefEffect(spyRandomBot);
        assertEquals(spyRandomBot, bishop.getSavedThief());
    }

    /**
     * On vérifie que le randomBot qui est un voleur ne peut pas utiliser l'effet sur un assassin.
     */
    @Test
    void randomBotUseEffectOfTheThiefWhenNoOpponentsAvailableTest() {
        Player player = spy(new RandomBot(2, deck, view, bank));
        Thief thief = spy(new Thief());
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(thief)));

        Assassin assassin = spy(new Assassin());
        player.chooseCharacter(new ArrayList<>(List.of(assassin)));

        when(spyRandomBot.getCharacter()).thenReturn(thief);
        when(spyRandomBot.getAvailableCharacters()).thenReturn(List.of(assassin));

        spyRandomBot.getCharacter().applyEffect();

        verify(view, times(0)).displayPlayerUseThiefEffect(spyRandomBot);
        assertNull(assassin.getSavedThief());
    }

    @Test
    void testChooseColorCourtyardOfMiracle() {
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextInt(DistrictType.values().length)).thenReturn(DistrictType.SOLDIERLY.ordinal());
        when(spyRandomBot.getCitadel()).thenReturn(new ArrayList<>(List.of(new Card(District.COURTYARD_OF_MIRACLE))));
        // Set the mockRandom in the RandomBot for testing
        spyRandomBot.setRandom(mockRandom);
        spyRandomBot.chooseColorCourtyardOfMiracle();
        DistrictType districtType = spyRandomBot.getColorCourtyardOfMiracleType();
        assertEquals(DistrictType.SOLDIERLY, districtType);

    }

    /**
     * Tester la méthode chooseWhenToPickACard et voir quand il choisit de piocher avant de jouer si
     * la méthode check est appelée
     */
    @Test
    void chooseWhenToPickACardWhenPickBeforePlaying() {
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(true);
        spyRandomBot.setRandom(mockRandom);
        spyRandomBot.setCitadel(List.of(new Card(District.LIBRARY)));
        Bishop bishop = spy(new Bishop());
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(bishop)));
        assertFalse(spyRandomBot.hasPlayed());
        spyRandomBot.chooseWhenToPickACard(1);
        assertTrue(spyRandomBot.hasPlayed());
        verify(spyRandomBot, times(1)).pickBeforePlaying(1);
        verify(spyRandomBot, times(0)).playBeforePicking(1);
    }

    /**
     * Tester la méthode chooseWhenToPickACard et voir quand il choisit de jouer avant de piocher si
     * la méthode check n'est pas appelé
     */
    @Test
    void chooseWhenToPickACardWhenPlayBeforePicking() {
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(false);
        spyRandomBot.setRandom(mockRandom);
        spyRandomBot.setCitadel(List.of(new Card(District.LIBRARY)));
        Bishop bishop = spy(new Bishop());
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(bishop)));
        spyRandomBot.chooseWhenToPickACard(1);
        assertTrue(spyRandomBot.hasPlayed());
        verify(spyRandomBot, times(0)).pickBeforePlaying(1);
        verify(spyRandomBot, times(1)).playBeforePicking(1);
    }

    @Test
    void pickBeforePlayingRandomBotUseCheckAndUseLibraryEffectInCitadelTest() {
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(false);
        spyRandomBot.setRandom(mockRandom);
        spyRandomBot.setCitadel(List.of(new Card(District.LIBRARY)));
        Bishop bishop = spy(new Bishop());
        spyRandomBot.chooseCharacter(new ArrayList<>(List.of(bishop)));
        spyRandomBot.pickBeforePlaying(1);
        verify(spyRandomBot, atLeastOnce()).isLibraryPresent();
    }

    @Test
    void wantToUseCemeteryEffectWithRandomTrueButNotEnoughGoldShouldReturnFalse() {
        Card temple = new Card(District.TEMPLE);
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(true);
        spyRandomBot.setRandom(mockRandom);
        when(spyRandomBot.getNbGold()).thenReturn(0);
        assertFalse(spyRandomBot.wantToUseCemeteryEffect(temple));
    }

    @Test
    void wantToUseCemeteryEffectWithEnoughGoldButRandomFalseShouldReturnFalse() {
        Card temple = new Card(District.TEMPLE);
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(false);
        spyRandomBot.setRandom(mockRandom);
        when(spyRandomBot.getNbGold()).thenReturn(1);
        assertFalse(spyRandomBot.wantToUseCemeteryEffect(temple));
    }

    @Test
    void wantToUseCemeteryEffectWithEnoughGoldAndRandomTrueShouldReturnTrue() {
        Card temple = new Card(District.TEMPLE);
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextBoolean()).thenReturn(true);
        spyRandomBot.setRandom(mockRandom);
        when(spyRandomBot.getNbGold()).thenReturn(1);
        assertTrue(spyRandomBot.wantToUseCemeteryEffect(temple));
    }
}