package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

/**
 * Observer che riceve notifiche sugli eventi di gioco nel momento in cui si verificano.
 * Registrare le istanze su {@link BattleEngine} per reagire agli esiti del combattimento
 * senza accoppiare la logica di gioco a nessuno strato di presentazione.
 */
public interface GameEventListener {

    void onUnitMoved(Unit unit, Position from, Position to);

    void onUnitAttacked(Unit attacker, Unit target, int damage);

    void onUnitDefeated(Unit unit);

    void onTurnChanged(Team newTeam, int turnNumber);
}
