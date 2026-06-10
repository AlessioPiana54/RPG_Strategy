package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

import java.util.Objects;

/** Incapsula un movimento di unità in modo che possa essere eseguito e annullato. */
public final class MoveCommand implements GameCommand {

    private final Unit unit;
    private final Position target;
    private final BattleEngine engine;
    private final GridMap map;
    private Position previousPosition;

    public MoveCommand(Unit unit, Position target, BattleEngine engine, GridMap map) {
        this.unit   = Objects.requireNonNull(unit,   "unit");
        this.target = Objects.requireNonNull(target, "target");
        this.engine = Objects.requireNonNull(engine, "engine");
        this.map    = Objects.requireNonNull(map,    "map");
    }

    @Override
    public void execute() {
        previousPosition = unit.getPosizione();
        engine.executeMove(unit, target, map);
    }

    @Override
    public void undo() {
        if (previousPosition == null)
            throw new IllegalStateException("Il comando non è ancora stato eseguito");
        engine.executeMove(unit, previousPosition, map);
        previousPosition = null;
    }
}
