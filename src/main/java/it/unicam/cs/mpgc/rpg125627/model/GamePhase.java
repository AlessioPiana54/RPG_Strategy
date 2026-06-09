package it.unicam.cs.mpgc.rpg125627.model;

/**
 * Rappresenta la fase corrente di una battaglia.
 */
public enum GamePhase {

    /** Il giocatore sta scegliendo le azioni per le proprie unità. */
    PLAYER_TURN,

    /** L'IA sta risolvendo le azioni delle unità nemiche. */
    ENEMY_TURN,

    /** Tutte le unità nemiche sono state sconfitte: il giocatore vince. */
    VICTORY,

    /** Tutte le unità del giocatore sono state sconfitte: il giocatore perde. */
    DEFEAT
}
