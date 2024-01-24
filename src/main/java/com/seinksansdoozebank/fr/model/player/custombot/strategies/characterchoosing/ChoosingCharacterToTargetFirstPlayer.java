package com.seinksansdoozebank.fr.model.player.custombot.strategies.characterchoosing;

import com.seinksansdoozebank.fr.model.character.abstracts.Character;
import com.seinksansdoozebank.fr.model.character.roles.Role;
import com.seinksansdoozebank.fr.model.player.Opponent;
import com.seinksansdoozebank.fr.model.player.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.seinksansdoozebank.fr.model.player.custombot.strategies.StrategyUtils.getCharacterFromRoleInLIst;
import static com.seinksansdoozebank.fr.model.player.custombot.strategies.StrategyUtils.isRoleInCharacterList;

/**
 * Represents a strategy to choose a character by taking the one that will be the
 * most useful to slow down the player who is the closest to win
 */
public class ChoosingCharacterToTargetFirstPlayer implements ICharacterChoosingStrategy {

    private static final int NB_GOLD_MIN_FOR_MANY_GOLD = 3;
    private final Random random;

    /**
     * Creates a new instance of ChoosingCharacterToTargetFirstPlayer
     */
    public ChoosingCharacterToTargetFirstPlayer() {
        this.random = new Random();
    }

    /**
     * Creates a new instance of ChoosingCharacterToTargetFirstPlayer with a given random to apply dependency injection
     *
     * @param random the random to use
     */
    public ChoosingCharacterToTargetFirstPlayer(Random random) {
        this.random = random;
    }

    @Override
    public Character apply(Player player, List<Character> characters) {
        if (isRoleInCharacterList(Role.CONDOTTIERE, characters)) {              //si le condottiere est disponible
            return getCharacterFromRoleInLIst(Role.CONDOTTIERE, characters);     // on le prend pour détruire un quartier du leadingOpponent
        }
        Opponent leadingOpponent = getLeadingOpponent(player);                  //on récupère le leadingOpponent pour regarder som nombre de pièces
        if (leadingOpponent.getNbGold() > NB_GOLD_MIN_FOR_MANY_GOLD) {          // s'il a beaucoup de pièces
            if (isRoleInCharacterList(Role.THIEF, characters)) {                //      et si le voleur est disponible
                return getCharacterFromRoleInLIst(Role.THIEF, characters);       //          on le prend pour le voler (car en ayant beaucoup de pièces, il va probablement construire)
            } else if (isRoleInCharacterList(Role.ARCHITECT, characters)) {     //      sinon, si l'architecte est disponible
                return getCharacterFromRoleInLIst(Role.ARCHITECT, characters);   //          on le prend pour l'empêcher de construire avec toutes ses pièces
            }
        } else {                                                                // sinon
            if (isRoleInCharacterList(Role.ASSASSIN, characters)) {             //      si l'assassin est disponible
                return getCharacterFromRoleInLIst(Role.ASSASSIN, characters);    //          on le prend pour tuer le marchand (car en ayant peu de pièces, il va probablement essayer d'en gagner)
            } else if (isRoleInCharacterList(Role.MERCHANT, characters)) {      //          si le marchand est disponible
                return getCharacterFromRoleInLIst(Role.MERCHANT, characters);    //              on le prend pour empêcher le leadingOpponent de gagner des pièces
            }
        }
        if (isRoleInCharacterList(Role.KING, characters)) {                     //si rien de tout ça n'est disponible, on essaye de prendre le roi pour
            return getCharacterFromRoleInLIst(Role.KING, characters);            // pouvoir choisir en premier au prochain tour
        }
        return characters.get(this.random.nextInt(characters.size()));
    }

    Opponent getLeadingOpponent(Player player) {
        Optional<Opponent> optionalOpponent = player.getOpponents().stream().filter(o -> !o.equals(player)).max(Comparator.comparingInt(Opponent::nbDistrictsInCitadel));
        if (optionalOpponent.isPresent()) {
            return optionalOpponent.get();
        } else {
            throw new IllegalStateException("No leading opponent found");
        }
    }
}