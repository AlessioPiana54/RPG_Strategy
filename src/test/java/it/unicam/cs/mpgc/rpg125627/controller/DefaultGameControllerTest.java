package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.GamePhase;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.MeleeAttack;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import it.unicam.cs.mpgc.rpg125627.model.UnitClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultGameControllerTest {

    private GameState state;
    private BattleEngine engine;
    private DefaultGameController controller;
    private Unit player, enemy;

    @BeforeEach
    void setUp() {
        GridMap map = new GridMap(8, 8);
        player = new Unit("Hero",   UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        enemy  = new Unit("Goblin", UnitClass.WARRIOR, Team.ENEMY,  new Position(7, 7));
        player.addAbilita(new MeleeAttack("Slash", 10));
        enemy.addAbilita(new MeleeAttack("Bite",  10));
        map.placeUnit(player, player.getPosizione());
        map.placeUnit(enemy,  enemy.getPosizione());
        state      = new GameState(map, List.of(player, enemy));
        engine     = new BattleEngine();
        controller = new DefaultGameController(state, engine, new SimpleAIStrategy());
    }

    // ── selectUnit ───────────────────────────────────────────────────────────

    @Test
    void selectPlayerUnitOnPlayerTurn() {
        controller.selectUnit(player.getPosizione());
        assertEquals(player, controller.getSelectedUnit());
    }

    @Test
    void cannotSelectEnemyUnitOnPlayerTurn() {
        controller.selectUnit(enemy.getPosizione());
        assertNull(controller.getSelectedUnit());
    }

    @Test
    void selectingNewUnitResetsMovedAndActedFlags() {
        controller.selectUnit(player.getPosizione());
        controller.moveSelectedUnit(new Position(0, 1));

        // Re-select (same unit, new selection resets flags)
        controller.selectUnit(player.getPosizione());
        // Now moving again should succeed if within range from current pos
        Position beforeSecondMove = player.getPosizione();
        controller.moveSelectedUnit(new Position(0, 2));
        assertEquals(new Position(0, 2), player.getPosizione());
        assertNotEquals(beforeSecondMove, player.getPosizione());
    }

    // ── moveSelectedUnit ─────────────────────────────────────────────────────

    @Test
    void moveUpdatesUnitPosition() {
        controller.selectUnit(player.getPosizione());
        controller.moveSelectedUnit(new Position(0, 1));
        assertEquals(new Position(0, 1), player.getPosizione());
    }

    @Test
    void cannotMoveTwiceWithoutReselect() {
        controller.selectUnit(player.getPosizione());
        controller.moveSelectedUnit(new Position(0, 1));
        Position posAfterFirstMove = player.getPosizione();

        controller.moveSelectedUnit(new Position(0, 2)); // must be ignored

        assertEquals(posAfterFirstMove, player.getPosizione());
    }

    @Test
    void moveDoesNothingWithoutSelection() {
        controller.moveSelectedUnit(new Position(0, 1)); // no unit selected
        assertEquals(new Position(0, 0), player.getPosizione());
    }

    // ── useAbility ───────────────────────────────────────────────────────────

    @Test
    void useAbilityOnAdjacentEnemy() {
        // Move player next to enemy-like target — use a nearby enemy for the test
        GridMap map2 = new GridMap(4, 4);
        Unit attacker = new Unit("A", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        Unit defender = new Unit("D", UnitClass.WARRIOR, Team.ENEMY,  new Position(0, 1));
        attacker.addAbilita(new MeleeAttack("Hit", 10));
        map2.placeUnit(attacker, attacker.getPosizione());
        map2.placeUnit(defender, defender.getPosizione());
        GameState state2 = new GameState(map2, List.of(attacker, defender));
        DefaultGameController ctrl2 = new DefaultGameController(state2, engine, new SimpleAIStrategy());

        int hpBefore = defender.getHp();
        ctrl2.selectUnit(attacker.getPosizione());
        ctrl2.useAbility(attacker.getAbilita().get(0), defender.getPosizione());

        assertTrue(defender.getHp() < hpBefore);
    }

    @Test
    void cannotActTwiceWithSameUnit() {
        GridMap map2 = new GridMap(4, 4);
        Unit attacker = new Unit("A", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        Unit defender = new Unit("D", UnitClass.WARRIOR, Team.ENEMY,  new Position(0, 1));
        attacker.addAbilita(new MeleeAttack("Hit", 5));
        map2.placeUnit(attacker, attacker.getPosizione());
        map2.placeUnit(defender, defender.getPosizione());
        GameState state2 = new GameState(map2, List.of(attacker, defender));
        DefaultGameController ctrl2 = new DefaultGameController(state2, engine, new SimpleAIStrategy());

        ctrl2.selectUnit(attacker.getPosizione());
        ctrl2.useAbility(attacker.getAbilita().get(0), defender.getPosizione());
        int hpAfterFirst = defender.getHp();

        ctrl2.useAbility(attacker.getAbilita().get(0), defender.getPosizione()); // must be ignored

        assertEquals(hpAfterFirst, defender.getHp());
    }

    // ── endTurn ──────────────────────────────────────────────────────────────

    @Test
    void endTurnReturnsToPlayerPhase() {
        controller.endTurn();
        assertEquals(GamePhase.PLAYER_TURN, state.getFase());
    }

    @Test
    void endTurnIncreasesTurnCounter() {
        int before = state.getTurnoCorrente();
        controller.endTurn();
        assertEquals(before + 1, state.getTurnoCorrente());
    }

    @Test
    void endTurnClearsSelection() {
        controller.selectUnit(player.getPosizione());
        controller.endTurn();
        assertNull(controller.getSelectedUnit());
    }

    @Test
    void endTurnDoesNothingWhenGameIsOver() {
        // Kill enemy so the game is already in VICTORY state
        enemy.takeDamage(enemy.getMaxHp());
        state.checkVictory();
        assertTrue(state.isOver());

        int turnBefore = state.getTurnoCorrente();
        controller.endTurn();
        assertEquals(turnBefore, state.getTurnoCorrente());
    }

    // ── startGame ────────────────────────────────────────────────────────────

    @Test
    void startGameFiresTurnChangedEvent() {
        List<String> captured = new ArrayList<>();
        engine.addListener(new GameEventListener() {
            public void onUnitMoved(Unit u, Position f, Position t) {}
            public void onUnitAttacked(Unit a, Unit t, int d) {}
            public void onUnitDefeated(Unit u) {}
            public void onTurnChanged(Team team, int turn) { captured.add(team.name()); }
        });
        controller.startGame();
        assertTrue(captured.contains("PLAYER"));
    }

    // ── undoLastCommand ──────────────────────────────────────────────────────

    @Test
    void undoMoveRevertsPosition() {
        controller.selectUnit(player.getPosizione());
        Position original = player.getPosizione();
        controller.moveSelectedUnit(new Position(0, 1));

        boolean undone = controller.undoLastCommand();

        assertTrue(undone);
        assertEquals(original, player.getPosizione());
    }

    @Test
    void undoReturnsFalseWhenHistoryIsEmpty() {
        assertFalse(controller.undoLastCommand());
    }

    // ── getGameState ─────────────────────────────────────────────────────────

    @Test
    void getGameStateReturnsSameInstance() {
        assertSame(state, controller.getGameState());
    }
}
