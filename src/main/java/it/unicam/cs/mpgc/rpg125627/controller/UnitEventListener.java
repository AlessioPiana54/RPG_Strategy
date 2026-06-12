package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

/**
 * Observer per gli eventi che riguardano le singole unità (movimento, attacco, sconfitta).
 * Tutti i metodi hanno un'implementazione di default vuota: un listener può ridefinire
 * solo gli eventi di cui ha bisogno senza dovere implementare quelli restanti.
 */
public interface UnitEventListener {

    default void onUnitMoved(Unit unit, Position from, Position to) {}

    default void onUnitAttacked(Unit attacker, Unit target, int damage) {}

    default void onUnitDefeated(Unit unit) {}
}
