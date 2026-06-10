package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import it.unicam.cs.mpgc.rpg125627.model.UnitClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnManagerTest {

    private Unit warrior, mage, goblin;
    private GameState state;

    @BeforeEach
    void setUp() {
        GridMap map = new GridMap(5, 5);
        warrior = new Unit("Warrior", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        mage    = new Unit("Mage",    UnitClass.MAGE,    Team.PLAYER, new Position(0, 1));
        goblin  = new Unit("Goblin",  UnitClass.WARRIOR, Team.ENEMY,  new Position(4, 4));
        map.placeUnit(warrior, warrior.getPosizione());
        map.placeUnit(mage,    mage.getPosizione());
        map.placeUnit(goblin,  goblin.getPosizione());
        state = new GameState(map, List.of(warrior, mage, goblin));
    }

    @Test
    void initialTeamIsPlayer() {
        TurnManager tm = new TurnManager(state);
        assertEquals(Team.PLAYER, tm.getCurrentTeam());
    }

    @Test
    void playerUnitsBeforeEnemyUnits() {
        TurnManager tm = new TurnManager(state);
        assertEquals(warrior, tm.getCurrentUnit());
        tm.nextUnit();
        assertEquals(mage, tm.getCurrentUnit());
        tm.nextUnit();
        assertEquals(goblin, tm.getCurrentUnit());
        assertEquals(Team.ENEMY, tm.getCurrentTeam());
    }

    @Test
    void nextUnitReturnsNullWhenRoundIsOver() {
        TurnManager tm = new TurnManager(state);
        tm.nextUnit(); // past warrior
        tm.nextUnit(); // past mage
        tm.nextUnit(); // past goblin
        assertNull(tm.getCurrentUnit());
        assertFalse(tm.hasMoreUnits());
    }

    @Test
    void resetRebuildsTurnOrder() {
        TurnManager tm = new TurnManager(state);
        tm.nextUnit();
        tm.nextUnit();
        tm.nextUnit();

        tm.resetTurn();

        assertEquals(warrior, tm.getCurrentUnit());
        assertTrue(tm.hasMoreUnits());
    }

    @Test
    void deadUnitsAreSkippedAutomatically() {
        warrior.takeDamage(warrior.getMaxHp()); // kill warrior
        TurnManager tm = new TurnManager(state);
        // warrior is dead so it should not appear in round order
        assertEquals(mage, tm.getCurrentUnit());
    }

    @Test
    void unitThatDiesMidRoundIsSkipped() {
        TurnManager tm = new TurnManager(state);
        assertEquals(warrior, tm.getCurrentUnit());
        // kill mage between turns
        mage.takeDamage(mage.getMaxHp());
        tm.nextUnit(); // should skip dead mage
        assertEquals(goblin, tm.getCurrentUnit());
    }
}
