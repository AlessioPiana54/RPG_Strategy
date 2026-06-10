package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Ability;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.Position;

/**
 * Punto d'ingresso per tutte le azioni di gioco, utilizzabile sia dal giocatore (livello UI)
 * sia dalle strategie IA senza alcuna dipendenza dalla tecnologia di presentazione.
 */
public interface GameController {

    /** Inizializza il controller e notifica i listener che la partita è iniziata. */
    void startGame();

    /**
     * Seleziona l'unità in {@code position} come unità attiva per i successivi
     * comandi di movimento e abilità. Possono essere selezionate solo le unità
     * appartenenti al team il cui turno è in corso.
     */
    void selectUnit(Position position);

    /** Sposta l'unità attualmente selezionata verso {@code position}. */
    void moveSelectedUnit(Position position);

    /**
     * Usa {@code ability} puntando all'unità o alla cella in {@code targetPosition}.
     * Avvia un controllo delle condizioni di vittoria al termine dell'azione.
     */
    void useAbility(Ability ability, Position targetPosition);

    /**
     * Termina il turno del team corrente. Se era il turno del giocatore, la IA nemica
     * esegue le proprie mosse automaticamente prima che il controllo torni al giocatore.
     */
    void endTurn();

    GameState getGameState();
}
