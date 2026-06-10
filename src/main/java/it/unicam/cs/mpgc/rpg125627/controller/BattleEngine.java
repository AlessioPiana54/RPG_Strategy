package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Ability;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.HealAbility;
import it.unicam.cs.mpgc.rpg125627.model.MeleeAttack;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.RangedAttack;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Esegue le azioni di movimento e uso delle abilità, calcola gli esiti in base alle
 * statistiche delle unità e notifica gli observer {@link GameEventListener} registrati.
 *
 * <p>Questa classe è volutamente priva di dipendenze da JavaFX, in modo da poter essere
 * testata in isolamento con semplici test JUnit.</p>
 */
public class BattleEngine {

    private final List<GameEventListener> listeners = new ArrayList<>();

    // ── Gestione dei listener ────────────────────────────────────────────────

    public void addListener(GameEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
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
     * Applica {@code ability} da {@code attacker} a {@code target}, valida la gittata
     * e notifica {@code onUnitAttacked} (e {@code onUnitDefeated} quando il bersaglio muore).
     * Il danno è ricavato dai parametri dell'abilità stessa.
     *
     * @throws IllegalArgumentException se il bersaglio è fuori gittata o il vincolo
     *                                  di team dell'abilità viene violato
     */
    public void executeAbility(Unit attacker, Ability ability, Unit target, GridMap map) {
        Objects.requireNonNull(attacker, "attacker");
        Objects.requireNonNull(ability,  "ability");
        Objects.requireNonNull(target,   "target");
        Objects.requireNonNull(map,      "map");

        validateAbilityRange(attacker, ability, target);

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
    }

    /** Notifica {@code onTurnChanged} a tutti i listener registrati. */
    public void fireTurnChanged(Team team, int turnNumber) {
        for (GameEventListener l : listeners) {
            l.onTurnChanged(team, turnNumber);
        }
    }

    // ── Validazione ──────────────────────────────────────────────────────────

    private void validateAbilityRange(Unit attacker, Ability ability, Unit target) {
        int distance = attacker.getPosizione().distanceTo(target.getPosizione());
        switch (ability) {
            case MeleeAttack _ -> {
                if (distance > 1)
                    throw new IllegalArgumentException(
                        "Il bersaglio è troppo lontano per un attacco corpo a corpo (distanza=" + distance + ")");
            }
            case RangedAttack r -> {
                if (distance > r.getGittata())
                    throw new IllegalArgumentException(
                        "Il bersaglio è fuori gittata (distanza=" + distance
                        + ", gittata=" + r.getGittata() + ")");
            }
            case HealAbility _ -> {
                if (attacker.getTeam() != target.getTeam())
                    throw new IllegalArgumentException(
                        "Non è possibile curare un'unità del team avversario");
            }
        }
    }

    // ── Notifica degli eventi ────────────────────────────────────────────────

    private void fireUnitMoved(Unit unit, Position from, Position to) {
        for (GameEventListener l : listeners) l.onUnitMoved(unit, from, to);
    }

    private void fireUnitAttacked(Unit attacker, Unit target, int damage) {
        for (GameEventListener l : listeners) l.onUnitAttacked(attacker, target, damage);
    }

    private void fireUnitDefeated(Unit unit) {
        for (GameEventListener l : listeners) l.onUnitDefeated(unit);
    }
}
