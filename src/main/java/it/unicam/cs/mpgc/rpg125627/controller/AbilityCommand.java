package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Ability;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

import java.util.Objects;

/** Incapsula l'uso di una singola abilità come comando (senza annullamento — le variazioni di PV non vengono ripristinate). */
public final class AbilityCommand implements GameCommand {

    private final Unit attacker;
    private final Ability ability;
    private final Unit target;
    private final BattleEngine engine;
    private final GridMap map;

    public AbilityCommand(Unit attacker, Ability ability, Unit target,
                          BattleEngine engine, GridMap map) {
        this.attacker = Objects.requireNonNull(attacker, "attacker");
        this.ability  = Objects.requireNonNull(ability,  "ability");
        this.target   = Objects.requireNonNull(target,   "target");
        this.engine   = Objects.requireNonNull(engine,   "engine");
        this.map      = Objects.requireNonNull(map,      "map");
    }

    @Override
    public void execute() {
        engine.executeAbility(attacker, ability, target, map);
    }
}
