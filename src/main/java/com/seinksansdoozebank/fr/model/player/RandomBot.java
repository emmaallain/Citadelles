package com.seinksansdoozebank.fr.model.player;

import com.seinksansdoozebank.fr.model.cards.Deck;
import com.seinksansdoozebank.fr.model.cards.District;
import com.seinksansdoozebank.fr.view.IView;

import java.util.Optional;

public class RandomBot extends Player {

    public RandomBot(int nbGold, Deck deck, IView view) {
        super(nbGold, deck, view);
    }

    @Override
    public void play() {
        view.displayPlayerStartPlaying(this);
        view.displayPlayerInfo(this);
        pickSomething();
        view.displayPlayerPlaysDistrict(this, this.buildADistrict());
    }

    @Override
    protected void pickSomething() {
        if (random.nextInt(2) == 0) {
            pickGold();
        } else {
            pickADistrict();
        }
    }

    @Override
    protected void pickADistrict() {
        this.view.displayPlayerPickDistrict(this);
        District district1 = deck.pick();
        District district2 = deck.pick();
        int randomChoice = random.nextInt(2);
        if (randomChoice == 0) {
            this.hand.add(district1);
            this.deck.discard(district2);
        } else {
            this.hand.add(district2);
            this.deck.discard(district1);
        }
    }

    @Override
    protected Optional<District> chooseDistrict() {
        if (!this.hand.isEmpty()) {
            District chosenDistrict;
            int cnt = 0;
            do {
                chosenDistrict = this.hand.get(random.nextInt(hand.size()));
                cnt++;
            } while (this.canBuildDistrict(chosenDistrict) && cnt < 5);
            if (this.canBuildDistrict(chosenDistrict)) {
                return Optional.of(chosenDistrict);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "Le bot aléatoire "+this.id;
    }
}
