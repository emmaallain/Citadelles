package com.seinksansdoozebank.fr.model.character.singleton;

import com.seinksansdoozebank.fr.model.cards.District;
import com.seinksansdoozebank.fr.model.cards.DistrictType;
import com.seinksansdoozebank.fr.model.character.abstracts.CommonCharacter;
import com.seinksansdoozebank.fr.model.character.roles.Role;
import com.seinksansdoozebank.fr.model.player.Player;

import java.util.List;

public class King extends CommonCharacter {

    private final Player player;
    private final List<District> citadel;
    private static final Role role = Role.KING;
    private static final DistrictType target = DistrictType.NOBILITY;

    public King(List<District> citadel, Player player) {
        super(role);
        this.citadel = citadel;
        this.player = player;
    }

    @Override
    public void useEffect() {
        // Nothing to do
    }

    /**
     * For each district in the citadel of the target type, the player will collect one gold
     */
    @Override
    public void goldCollectedFromDisctrictType() {
        int nbGold = 0;
        for (District district : this.citadel) {
            if (district.getDistrictType() == target) {
                nbGold++;
            }
        }
        this.player.increaseGold(nbGold);
    }

    /**
     * Method called to perform the action of the character
     * This method will ask the user to choose an action to perform
     * The action will be performed by the character
     */
    @Override
    public void performAction() {
        this.goldCollectedFromDisctrictType();
    }
}
