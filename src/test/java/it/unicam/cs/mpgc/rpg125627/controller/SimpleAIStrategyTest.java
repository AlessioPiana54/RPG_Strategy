package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.MeleeAttack;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.RangedAttack;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import it.unicam.cs.mpgc.rpg125627.model.UnitClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleAIStrategyTest {

    private GridMap map;
    private BattleEngine engine;
    private SimpleAIStrategy ai;

    @BeforeEach
    void setUp() {
        map    = new GridMap(8, 8);
        engine = new BattleEngine();
        ai     = new SimpleAIStrategy();
    }

    /**
     * Advances {@code state} to ENEMY_TURN (required so that selectUnit accepts
     * enemy units) and invokes the AI strategy with a fresh controller.
     */
    private void runAiTurn(GameState state) {
        state.nextTurn(); // PLAYER_TURN → ENEMY_TURN
        ai.playTurn(state, new DefaultGameController(state, engine, ai));
    }

    // ── Movement ─────────────────────────────────────────────────────────────

    @Test
    void enemyMovesCloserToPlayer() {
        Unit player = new Unit("Player", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        Unit goblin = new Unit("Goblin", UnitClass.WARRIOR, Team.ENEMY,  new Position(0, 4));
        goblin.addAbilita(new MeleeAttack("Bite", 5));
        map.placeUnit(player, player.getPosizione());
        map.placeUnit(goblin, goblin.getPosizione());
        GameState state = new GameState(map, List.of(player, goblin));

        int distanceBefore = goblin.getPosizione().distanceTo(player.getPosizione());
        runAiTurn(state);
        int distanceAfter = goblin.getPosizione().distanceTo(player.getPosizione());

        assertTrue(distanceAfter < distanceBefore,
            "Enemy should move closer to player. Before=" + distanceBefore + " After=" + distanceAfter);
    }

    @Test
    void enemyDoesNotMoveBeyondMoveRange() {
        Unit player = new Unit("Player", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        Unit goblin = new Unit("Goblin", UnitClass.WARRIOR, Team.ENEMY,  new Position(0, 7));
        goblin.addAbilita(new MeleeAttack("Bite", 5));
        map.placeUnit(player, player.getPosizione());
        map.placeUnit(goblin, goblin.getPosizione());
        GameState state = new GameState(map, List.of(player, goblin));

        Position posBefore = goblin.getPosizione();
        runAiTurn(state);

        int moved = posBefore.distanceTo(goblin.getPosizione());
        assertTrue(moved <= UnitClass.WARRIOR.getMoveRange(),
            "Enemy moved further than its move range: " + moved);
    }

    // ── Attacking ────────────────────────────────────────────────────────────

    @Test
    void enemyAttacksWhenAdjacentToPlayer() {
        Unit player = new Unit("Player", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        Unit goblin = new Unit("Goblin", UnitClass.WARRIOR, Team.ENEMY,  new Position(0, 1));
        goblin.addAbilita(new MeleeAttack("Bite", 15));
        map.placeUnit(player, player.getPosizione());
        map.placeUnit(goblin, goblin.getPosizione());
        GameState state = new GameState(map, List.of(player, goblin));

        int hpBefore = player.getHp();
        runAiTurn(state);

        assertTrue(player.getHp() < hpBefore,
            "Enemy should have attacked the adjacent player unit");
    }

    @Test
    void enemyWithRangedAbilityAttacksFromDistance() {
        Unit player = new Unit("Player", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        Unit archer = new Unit("Archer", UnitClass.ARCHER,  Team.ENEMY,  new Position(0, 2));
        archer.addAbilita(new RangedAttack("Shot", 12, 3));
        map.placeUnit(player, player.getPosizione());
        map.placeUnit(archer, archer.getPosizione());
        GameState state = new GameState(map, List.of(player, archer));

        int hpBefore = player.getHp();
        runAiTurn(state);

        assertTrue(player.getHp() < hpBefore,
            "Ranged enemy should attack player within its range");
    }

    @Test
    void enemyDoesNotAttackWhenOutOfRange() {
        Unit player = new Unit("Player", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        Unit goblin = new Unit("Goblin", UnitClass.WARRIOR, Team.ENEMY,  new Position(0, 7));
        goblin.addAbilita(new MeleeAttack("Bite", 5));
        map.placeUnit(player, player.getPosizione());
        map.placeUnit(goblin, goblin.getPosizione());
        GameState state = new GameState(map, List.of(player, goblin));

        // After moving 3 cells goblin is at col 4 — still too far for melee
        int hpBefore = player.getHp();
        runAiTurn(state);

        assertEquals(hpBefore, player.getHp(),
            "Enemy should not attack when still out of melee range after moving");
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

    @Test
    void aiStopsWhenGameIsOver() {
        Unit player = new Unit("Player", UnitClass.MAGE,    Team.PLAYER, new Position(0, 0));
        Unit goblin = new Unit("Goblin", UnitClass.WARRIOR, Team.ENEMY,  new Position(0, 1));
        goblin.addAbilita(new MeleeAttack("Kill", 9999));
        map.placeUnit(player, player.getPosizione());
        map.placeUnit(goblin, goblin.getPosizione());
        GameState state = new GameState(map, List.of(player, goblin));

        // Should not throw even when the kill ends the game mid-loop
        assertDoesNotThrow(() -> runAiTurn(state));
        assertTrue(state.isOver());
    }

    @Test
    void aiSkipsDeadEnemies() {
        Unit player  = new Unit("Player",  UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        Unit goblin1 = new Unit("Goblin1", UnitClass.WARRIOR, Team.ENEMY,  new Position(3, 3));
        Unit goblin2 = new Unit("Goblin2", UnitClass.WARRIOR, Team.ENEMY,  new Position(3, 4));
        goblin1.addAbilita(new MeleeAttack("Bite", 5));
        goblin2.addAbilita(new MeleeAttack("Bite", 5));
        goblin1.takeDamage(goblin1.getMaxHp()); // pre-kill goblin1
        map.placeUnit(player,  player.getPosizione());
        map.placeUnit(goblin2, goblin2.getPosizione());
        // goblin1 is dead — not placed on map
        GameState state = new GameState(map, List.of(player, goblin1, goblin2));

        assertDoesNotThrow(() -> runAiTurn(state));
    }
}
