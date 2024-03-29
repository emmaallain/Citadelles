package com.seinksansdoozebank.fr.model.player.custombot.strategies.picking;

import com.seinksansdoozebank.fr.model.player.Player;

/**
 * Implementation of the picking strategy where the player always pick gold
 */
public class PickingAlwaysGold implements IPickingStrategy {
    @Override
    public void apply(Player player) {
        player.pickGold();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PickingAlwaysGold;
    }

    @Override
    public int hashCode() {
        return PickingAlwaysGold.class.getName().hashCode();
    }
}
