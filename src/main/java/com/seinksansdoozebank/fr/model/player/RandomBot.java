package com.seinksansdoozebank.fr.model.player;

import com.seinksansdoozebank.fr.model.bank.Bank;
import com.seinksansdoozebank.fr.model.cards.Card;
import com.seinksansdoozebank.fr.model.cards.Deck;
import com.seinksansdoozebank.fr.model.cards.District;
import com.seinksansdoozebank.fr.model.cards.DistrictType;
import com.seinksansdoozebank.fr.model.character.abstracts.Character;
import com.seinksansdoozebank.fr.model.character.commoncharacters.WarlordTarget;
import com.seinksansdoozebank.fr.model.character.roles.Role;
import com.seinksansdoozebank.fr.model.character.specialscharacters.MagicianTarget;
import com.seinksansdoozebank.fr.view.IView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.shuffle;

/**
 * The RandomBot class represents a bot that makes random decisions.
 */
public class RandomBot extends Player {
    /**
     * RandomBot constructor
     *
     * @param nbGold the number of gold
     * @param deck   the deck
     * @param view   the view
     * @param bank   the bank
     */
    public RandomBot(int nbGold, Deck deck, IView view, Bank bank) {
        super(nbGold, deck, view, bank);
    }

    @Override
    public void playARound() {
        this.useCommonCharacterEffect();
        this.getCharacter().applyEffect();
        int nbDistrictsToBuild = random.nextInt(this.getNbDistrictsCanBeBuild() + 1);
        this.chooseWhenToPickACard(nbDistrictsToBuild);
    }

    /**
     * Choose when to pick a card
     *
     * @param nbDistrictsToBuild the number of districts the bot choose to build
     */
    public void chooseWhenToPickACard(int nbDistrictsToBuild) {
        if (random.nextBoolean()) {
            this.pickBeforePlaying(nbDistrictsToBuild);
        } else {
            this.playBeforePicking(nbDistrictsToBuild);
        }
    }

    /**
     * Represents the player's choice to pick something before playing
     *
     * @param nbDistrictsToBuild the number of districts the bot choose to build
     */
    protected void pickBeforePlaying(int nbDistrictsToBuild) {
        pickSomething();
        if (nbDistrictsToBuild > 0) {
            this.buyXCardsAndAddThemToCitadel(nbDistrictsToBuild);
        }
    }


    /**
     * Represents the player's choice to play something before picking
     *
     * @param nbDistrictsToBuild the number of districts the bot choose to build
     */
    protected void playBeforePicking(int nbDistrictsToBuild) {
        if (nbDistrictsToBuild > 0) {
            this.buyXCardsAndAddThemToCitadel(nbDistrictsToBuild);
        }
        pickSomething();
    }

    @Override
    protected void pickSomething() {
        if (random.nextBoolean()) {
            pickGold();
        } else {
            pickCardsKeepSomeAndDiscardOthers();
        }
    }

    /**
     * On choisit une carte aléatoire parmi celles proposées
     *
     * @param pickedCards the cards picked
     * @return the card that will be kept
     */
    @Override
    protected Card keepOneDiscardOthers(List<Card> pickedCards) {
        shuffle(pickedCards);
        return pickedCards.get(0);
    }

    @Override
    protected Optional<Card> chooseCard() {
        if (!this.hand.isEmpty()) {
            Card chosenCard;
            int cnt = 0;
            do {
                chosenCard = this.hand.get(random.nextInt(hand.size()));
                cnt++;
            } while (!this.canPlayCard(chosenCard) && cnt < 5);
            if (this.canPlayCard(chosenCard)) {
                return Optional.of(chosenCard);
            }
        }
        return Optional.empty();
    }

    @Override
    protected Character chooseCharacterImpl(List<Character> characters) {
        return characters.get(random.nextInt(characters.size()));
    }

    /**
     * The magician can exchange all it's card with another player or
     * can exchange some card with the same number of cards with the deck
     */
    @Override
    public MagicianTarget useEffectMagician() {
        // if the value is 0, the bot is not using the magician effect, else it is using it
        if (random.nextBoolean()) {
            // if true exchange all the card with a player
            if (random.nextBoolean()) {
                // get a random player
                Opponent opponentToExchangeCards = this.getOpponents().get(random.nextInt(this.getOpponents().size()));
                // exchange all the cards with the player
                this.view.displayPlayerUseMagicianEffect(this, opponentToExchangeCards);
                return new MagicianTarget(opponentToExchangeCards, List.of());
            }
            // if false exchange some cards with the deck
            // exchange some cards with the deck
            int nbCardsToExchange = random.nextInt(this.getHand().size() + 1);
            // Choose the cards from the district to exchange
            Collections.shuffle(this.getHand());
            List<Card> cardsToExchange = this.getHand().stream()
                    .limit(nbCardsToExchange)
                    .toList();
            this.view.displayPlayerUseMagicianEffect(this, null);
            return new MagicianTarget(null, cardsToExchange);
        }
        return null;
    }

    /**
     * Effect of assassin character (kill a player)
     */
    @Override
    public Character useEffectAssassin() {
        Character characterToKill = this.chooseAssassinTarget();
        // try to kill the playerToKill and if throw retry until the playerToKill is dead
        while (!characterToKill.isDead()) {
            try {
                view.displayPlayerUseAssassinEffect(this, characterToKill);
                return characterToKill;
            } catch (IllegalArgumentException e) {
                characterToKill = this.getAvailableCharacters().get(random.nextInt(this.getAvailableCharacters().size()));
            }
        }
        return null;
    }

    @Override
    protected Character chooseAssassinTarget() {
        List<Character> targetableCharacters = this.getAvailableCharacters().stream().filter(character -> character.getRole() != Role.ASSASSIN &&
                !character.isDead()).toList();
        return targetableCharacters.get(random.nextInt(targetableCharacters.size()));
    }

    @Override
    public WarlordTarget chooseWarlordTarget(List<Opponent> opponentsFocusable) {
        // if the value is 0, the bot is not using the warlord effect, else it is using it
        if (random.nextBoolean()) {
            Opponent opponentToDestroyDistrict = opponentsFocusable.get(random.nextInt(opponentsFocusable.size()));
            List<Card> opponentCitadel = opponentToDestroyDistrict.getCitadel()
                    .stream().filter(card -> !card.getDistrict().equals(District.DONJON)).toList();
            // if the player has no district, the bot will not use the warlord effect
            if (opponentCitadel.isEmpty()) {
                return null;
            }
            // get the random district
            int index = random.nextInt(opponentCitadel.size());
            // get the district to destroy
            District districtToDestroy = opponentCitadel.get(index).getDistrict();
            // Check if the number of golds of the player is enough to destroy the district
            if (this.getNbGold() >= districtToDestroy.getCost() - 1) {
                // destroy the district
                return new WarlordTarget(opponentToDestroyDistrict, districtToDestroy);
            }
        }
        return null;
    }

    protected Optional<Character> chooseThiefTarget() {
        List<Character> targetableCharacters = this.getAvailableCharacters().stream().filter(character -> character.getRole() != Role.ASSASSIN &&
                character.getRole() != Role.THIEF &&
                !character.isDead()).toList();
        if (targetableCharacters.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(targetableCharacters.get(random.nextInt(targetableCharacters.size())));
        }
    }


    public void chooseColorCourtyardOfMiracle() {
        // Set a random DistricType to the Courtyard of Miracle
        this.getCitadel().stream()
                .filter(card -> card.getDistrict().equals(District.COURTYARD_OF_MIRACLE))
                .findFirst()
                .ifPresent(card -> this.setColorCourtyardOfMiracleType(DistrictType.values()[random.nextInt(DistrictType.values().length)]));
    }

    @Override
    protected boolean wantToUseCemeteryEffect(Card card) {
        return random.nextBoolean() && this.getNbGold() > 0;

    }

    @Override
    public boolean wantToUseManufactureEffect() {
        return this.getNbGold() > 3 && random.nextBoolean();
    }

    @Override
    public String toString() {
        return "Le bot aléatoire " + this.id;
    }

    public Card chooseCardToDiscardForLaboratoryEffect() {
        return this.hand.get(random.nextInt(this.hand.size()));
    }
}
