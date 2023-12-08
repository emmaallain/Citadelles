package com.seinksansdoozebank.fr.controller;

import com.seinksansdoozebank.fr.model.cards.Deck;
import com.seinksansdoozebank.fr.model.cards.District;
import com.seinksansdoozebank.fr.model.player.Player;
import com.seinksansdoozebank.fr.view.Cli;
import com.seinksansdoozebank.fr.view.IView;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private static final int NB_GOLD_INIT = 30;
    private static final int NB_CARD_BY_PLAYER = 4;
    private static final int NB_ROUND = 4;
    Deck deck;
    List<Player> players;

    IView view;

    public Game(int nbPlayers) {
        this.view = new Cli();
        this.deck = new Deck();
        this.players = new ArrayList<>();
        for (int i = 0; i < nbPlayers; i++) {
            players.add(new Player(NB_GOLD_INIT, view));
        }
    }

    public void run() {
        this.init();
        boolean isGameFinished = false;
        int round = 0;
        while (!isGameFinished && round < NB_ROUND) {
            view.displayRound(round + 1);
            for (Player player : players) {
                District district = player.play();
                view.displayPlayerPlaysDistrict(player, district);
                view.displayPlayerHand(player, player.getHand());
                view.displayPlayerCitadel(player, player.getCitadel());
            }
            isGameFinished = players.stream().allMatch(player -> player.getHand().isEmpty());
            round++;
        }
        view.displayWinner(getWinner().toString(), getWinner().getScore());
    }


    private void init() {
        dealCards();
    }

    private void dealCards() {
        for (int i = 0; i < NB_CARD_BY_PLAYER; i++) {
            for (Player player : players) {
                player.getHand().add(deck.pick());
            }
        }
    }

    protected Player getWinner() {
        Player bestPlayer = players.get(0);
        for (Player currentPlayer : players) {
            if (currentPlayer.getScore() > bestPlayer.getScore()) {
                bestPlayer = currentPlayer;
            }
        }
        return bestPlayer;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;

    }
}
