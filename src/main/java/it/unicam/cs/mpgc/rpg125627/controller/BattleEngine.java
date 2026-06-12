package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Ability;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Esegue le azioni di movimento e uso delle abilità, calcola gli esiti in base alle
 * statistiche delle unità e notifica gli observer registrati.
 *
 * <p>Questa classe è volutamente priva di dipendenze da JavaFX, in modo da poter essere
 * testata in isolamento con semplici test JUnit.</p>
 */
public class BattleEngine {

    private final List<UnitEventListener> unitListeners = new ArrayList<>();
    private final List<TurnEventListener> turnListeners = new ArrayList<>();

    // ── Gestione dei listener ────────────────────────────────────────────────

    /** Registra un listener completo per tutti gli eventi di unità e di turno. */
    public void addListener(GameEventListener listener) {
        Objects.requireNonNull(listener, "listener");
        unitListeners.add(listener);
        turnListeners.add(listener);
    }

    /** Registra un listener interessato solo agli eventi di unità. */
    public void addUnitListener(UnitEventListener listener) {
        unitListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Registra un listener interessato solo ai cambi di turno. */
    public void addTurnListener(TurnEventListener listener) {
        turnListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(GameEventListener listener) {
        unitListeners.remove(listener);
        turnListeners.remove(listener);
    }

    // ── Azioni ───────────────────────────────────────────────────────────────

    /**
     * Sposta {@code unit} verso {@code target}, valida la distanza e la percorribilità,
     * libera la cella di origine e notifica {@code onUnitMoved}.
     *
     * @throws IllegalArgumentException se la destinazione è fuori dalla gittata di movimento o non percorribile
     */
    public void executeMove(Unit unit, Position target, GridMap map) {
        Objects.requireNonNull(unit,   "unit");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(map,    "map");

        int distance = unit.getPosizione().distanceTo(target);
        if (distance > unit.getClasseUnita().getMoveRange()) {
            throw new IllegalArgumentException(
                "Distanza di movimento " + distance + " supera la gittata "
                + unit.getClasseUnita().getMoveRange() + " di " + unit.getName());
        }
        if (!map.isWalkable(target)) {
            throw new IllegalArgumentException(
                "La posizione di destinazione " + target + " non è percorribile");
        }

        Position from = unit.getPosizione();
        map.removeUnit(from);
        map.placeUnit(unit, target);
        fireUnitMoved(unit, from, target);
    }

    /**
     * Applica {@code ability} da {@code attacker} a {@code target}, valida il bersaglio
     * tramite {@link Ability#isValidTarget} e notifica {@code onUnitAttacked}
     * (e {@code onUnitDefeated} quando il bersaglio muore).
     *
     * @throws IllegalArgumentException se il bersaglio non è valido per l'abilità
     */
    public void executeAbility(Unit attacker, Ability ability, Unit target, GridMap map) {
        Objects.requireNonNull(attacker, "attacker");
        Objects.requireNonNull(ability,  "ability");
        Objects.requireNonNull(target,   "target");
        Objects.requireNonNull(map,      "map");

        if (!ability.isValidTarget(attacker, target)) {
            throw new IllegalArgumentException(
                "Bersaglio non valido per " + attacker.getName()
                + " con abilità '" + ability.getName() + "'");
        }

        int hpBefore = target.getHp();
        attacker.useAbility(ability, target);
        int hpAfter = target.getHp();

        int damageDealt = hpBefore - hpAfter;
        if (damageDealt > 0) {
            fireUnitAttacked(attacker, target, damageDealt);
        }

        if (!target.isAlive()) {
            map.removeUnit(target.getPosizione());
            fireUnitDefeated(target);
        }

        if (!attacker.isAlive()) {
            map.removeUnit(attacker.getPosizione());
            fireUnitDefeated(attacker);
        }
    }

    /** Notifica {@code onTurnChanged} a tutti i listener registrati. */
    public void fireTurnChanged(Team team, int turnNumber) {
        for (TurnEventListener l : turnListeners) {
            l.onTurnChanged(team, turnNumber);
        }
    }

    // ── Notifica degli eventi ────────────────────────────────────────────────

    private void fireUnitMoved(Unit unit, Position from, Position to) {
        for (UnitEventListener l : unitListeners) l.onUnitMoved(unit, from, to);
    }

    private void fireUnitAttacked(Unit attacker, Unit target, int damage) {
        for (UnitEventListener l : unitListeners) l.onUnitAttacked(attacker, target, damage);
    }

    private void fireUnitDefeated(Unit unit) {
        for (UnitEventListener l : unitListeners) l.onUnitDefeated(unit);
    }
}
