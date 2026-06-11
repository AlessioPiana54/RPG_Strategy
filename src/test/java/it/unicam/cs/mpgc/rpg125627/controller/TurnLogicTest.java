package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.ActionState;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica la logica di turno basata su {@link ActionState}:
 * transizioni di stato, restrizioni di movimento/azione, reset e auto-endTurn.
 */
class TurnLogicTest {

    private Unit player;
    private Unit enemy;
    private GameState state;
    private DefaultGameController controller;

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
        controller = new DefaultGameController(state, new BattleEngine(), new SimpleAIStrategy());
    }

    // ── Stato iniziale ────────────────────────────────────────────────────────

    @Test
    void unitStartsAsReady() {
        assertEquals(ActionState.READY, player.getActionState());
    }

    // ── Transizioni READY → MOVED → EXHAUSTED ────────────────────────────────

    @Test
    void unitCanMoveAndActWhenReady() {
        controller.selectUnit(player.getPosizione());
        controller.moveSelectedUnit(new Position(0, 1));
        assertEquals(ActionState.MOVED, player.getActionState());
    }

    @Test
    void unitCannotMoveTwice() {
        // onMoved() lancia IllegalStateException se lo stato non è READY
        player.onMoved(); // READY → MOVED
        assertThrows(IllegalStateException.class, () -> player.onMoved());
    }

    @Test
    void unitBecomesExhaustedAfterActing() {
        player.onMoved();
        player.onActed();
        assertEquals(ActionState.EXHAUSTED, player.getActionState());
    }

    @Test
    void exhaustedUnitCannotMoveViaController() {
        player.onActed(); // READY → EXHAUSTED direttamente
        Position before = player.getPosizione();

        // Non è possibile selezionare un'unità EXHAUSTED
        controller.selectUnit(player.getPosizione());
        assertNull(controller.getSelectedUnit());

        // La posizione non deve cambiare
        assertEquals(before, player.getPosizione());
    }

    @Test
    void movedUnitCannotMoveAgainViaController() {
        controller.selectUnit(player.getPosizione());
        controller.moveSelectedUnit(new Position(0, 1));
        assertEquals(ActionState.MOVED, player.getActionState());

        Position afterFirstMove = player.getPosizione();

        // Un secondo moveSelectedUnit deve essere ignorato
        controller.moveSelectedUnit(new Position(0, 2));
        assertEquals(afterFirstMove, player.getPosizione());
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    @Test
    void resetForNewTurnRestoresReady() {
        player.onMoved();
        player.onActed();
        assertEquals(ActionState.EXHAUSTED, player.getActionState());

        player.resetForNewTurn();
        assertEquals(ActionState.READY, player.getActionState());
    }

    @Test
    void resetAllowsMovingAgain() {
        player.onMoved();
        player.resetForNewTurn();
        assertDoesNotThrow(player::onMoved);
    }

    // ── isEndTurnAvailable ───────────────────────────────────────────────────

    @Test
    void endTurnNotAvailableWhenUnitIsReady() {
        assertFalse(controller.isEndTurnAvailable());
    }

    @Test
    void endTurnNotAvailableWhenUnitIsMoved() {
        player.onMoved();
        assertFalse(controller.isEndTurnAvailable());
    }

    @Test
    void endTurnAvailableWhenAllPlayerUnitsExhausted() {
        player.onActed();
        assertTrue(controller.isEndTurnAvailable());
    }

    // ── After endTurn: tutte le unità tornano READY ──────────────────────────

    @Test
    void afterEndTurnAllUnitsAreReady() {
        // Esegui endTurn (l'IA nemica giocherà e poi il turno tornerà al giocatore)
        controller.endTurn();

        assertEquals(GamePhase.PLAYER_TURN, state.getFase());
        for (Unit u : state.getUnita()) {
            if (u.isAlive()) {
                assertEquals(ActionState.READY, u.getActionState(),
                    u.getName() + " dovrebbe essere READY dopo endTurn");
            }
        }
    }

    @Test
    void afterEndTurnTurnCounterIncreases() {
        int before = state.getTurnoCorrente();
        controller.endTurn();
        assertEquals(before + 1, state.getTurnoCorrente());
    }

    // ── Persistenza: onActed → EXHAUSTED su uso abilità ─────────────────────

    @Test
    void useAbilityExhaustsUnit() {
        // Setup ravvicinato per permettere l'attacco
        GridMap map2 = new GridMap(4, 4);
        Unit attacker = new Unit("A", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        Unit defender = new Unit("D", UnitClass.WARRIOR, Team.ENEMY,  new Position(0, 1));
        attacker.addAbilita(new MeleeAttack("Hit", 5));
        map2.placeUnit(attacker, attacker.getPosizione());
        map2.placeUnit(defender, defender.getPosizione());
        GameState state2 = new GameState(map2, List.of(attacker, defender));
        DefaultGameController ctrl2 = new DefaultGameController(state2, new BattleEngine(), new SimpleAIStrategy());

        ctrl2.selectUnit(attacker.getPosizione());
        ctrl2.useAbility(attacker.getAbilita().get(0), defender.getPosizione());

        assertEquals(ActionState.EXHAUSTED, attacker.getActionState());
    }
}
