package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.ActionState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

/**
 * Valida se un'unità può muoversi o agire nel turno corrente in base al suo {@link ActionState}.
 * Estratto da {@link DefaultGameController} per rispettare il principio di singola responsabilità.
 */
public class MoveValidator {

    /**
     * @return {@code true} se {@code unit} è in stato {@link ActionState#READY}
     *         e può ancora spostarsi verso {@code target}
     */
    public boolean canMove(Unit unit, Position target, GridMap map) {
        return unit != null && unit.getActionState() == ActionState.READY;
    }

    /**
     * @return {@code true} se {@code unit} non è {@link ActionState#EXHAUSTED}
     *         e può ancora usare un'abilità
     */
    public boolean canAct(Unit unit) {
        return unit != null && unit.getActionState() != ActionState.EXHAUSTED;
    }
}
