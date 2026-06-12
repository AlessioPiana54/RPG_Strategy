package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Team;

/**
 * Observer per i cambi di turno.
 * Il metodo ha un'implementazione di default vuota: un listener può ridefinirlo
 * senza dovere implementare gli eventi di unità definiti in {@link UnitEventListener}.
 */
public interface TurnEventListener {

    default void onTurnChanged(Team newTeam, int turnNumber) {}
}
