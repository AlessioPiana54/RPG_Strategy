package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.MeleeAttack;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.RangedAttack;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import it.unicam.cs.mpgc.rpg125627.model.UnitClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BattleEngineTest {

    /** Captures every event name for assertion. */
    static class RecordingListener implements GameEventListener {
        final List<String> events = new ArrayList<>();
        @Override public void onUnitMoved(Unit u, Position f, Position t)  { events.add("moved:"    + u.getName()); }
        @Override public void onUnitAttacked(Unit a, Unit t, int d)        { events.add("attacked:" + a.getName() + ">" + t.getName() + ":" + d); }
        @Override public void onUnitDefeated(Unit u)                       { events.add("defeated:" + u.getName()); }
        @Override public void onTurnChanged(Team team, int turn)           { events.add("turn:"     + team + ":" + turn); }
    }

    private GridMap map;
    private Unit hero, enemy;
    private BattleEngine engine;
    private RecordingListener listener;

    @BeforeEach
    void setUp() {
        map    = new GridMap(6, 6);
        hero   = new Unit("Hero",  UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        enemy  = new Unit("Enemy", UnitClass.WARRIOR, Team.ENEMY,  new Position(0, 1));
        hero.addAbilita(new MeleeAttack("Slash", 20));
        hero.addAbilita(new RangedAttack("Arrow", 15, 3));
        map.placeUnit(hero,  hero.getPosizione());
        map.placeUnit(enemy, enemy.getPosizione());
        engine   = new BattleEngine();
        listener = new RecordingListener();
        engine.addListener(listener);
    }

    // ── executeMove ──────────────────────────────────────────────────────────

    @Test
    void executeMoveUpdatesUnitPosition() {
        engine.executeMove(hero, new Position(1, 0), map);
        assertEquals(new Position(1, 0), hero.getPosizione());
    }

    @Test
    void executeMoveFreesOriginCell() {
        engine.executeMove(hero, new Position(1, 0), map);
        assertTrue(map.isWalkable(new Position(0, 0)));
    }

    @Test
    void executeMoveFiresMovedEvent() {
        engine.executeMove(hero, new Position(1, 0), map);
        assertTrue(listener.events.stream().anyMatch(e -> e.equals("moved:Hero")));
    }

    @Test
    void executeMoveRejectsExceedingMoveRange() {
        assertThrows(IllegalArgumentException.class,
            () -> engine.executeMove(hero, new Position(5, 5), map));
    }

    @Test
    void executeMoveRejectsNonWalkableTarget() {
        // enemy is at (0,1) — occupied and therefore not walkable
        assertThrows(IllegalArgumentException.class,
            () -> engine.executeMove(hero, new Position(0, 1), map));
    }

    // ── executeAbility ───────────────────────────────────────────────────────

    @Test
    void executeAbilityDealsCorrectDamage() {
        int hpBefore = enemy.getHp();
        engine.executeAbility(hero, hero.getAbilita().get(0), enemy, map);
        assertEquals(hpBefore - 20, enemy.getHp());
    }

    @Test
    void executeAbilityFiresAttackedEvent() {
        engine.executeAbility(hero, hero.getAbilita().get(0), enemy, map);
        assertTrue(listener.events.stream().anyMatch(e -> e.startsWith("attacked:Hero>Enemy:")));
    }

    @Test
    void executeAbilityFiresDefeatedWhenTargetKilled() {
        Unit weakFoe = new Unit("WeakFoe", UnitClass.MAGE, Team.ENEMY, new Position(0, 2));
        map.placeUnit(weakFoe, weakFoe.getPosizione());
        hero.addAbilita(new RangedAttack("Nuke", 9999, 3));
        var nuke = hero.getAbilita().get(2);

        engine.executeAbility(hero, nuke, weakFoe, map);

        assertFalse(weakFoe.isAlive());
        assertTrue(listener.events.stream().anyMatch(e -> e.equals("defeated:WeakFoe")));
    }

    @Test
    void executeAbilityRemovesDeadUnitFromMap() {
        Unit weakFoe = new Unit("WeakFoe", UnitClass.MAGE, Team.ENEMY, new Position(0, 2));
        map.placeUnit(weakFoe, weakFoe.getPosizione());
        hero.addAbilita(new RangedAttack("Nuke", 9999, 3));
        var nuke = hero.getAbilita().get(2);

        engine.executeAbility(hero, nuke, weakFoe, map);

        assertTrue(map.isWalkable(new Position(0, 2)));
    }

    @Test
    void executeAbilityRejectsMeleeOutOfRange() {
        Unit farFoe = new Unit("FarFoe", UnitClass.WARRIOR, Team.ENEMY, new Position(4, 4));
        map.placeUnit(farFoe, farFoe.getPosizione());
        assertThrows(IllegalArgumentException.class,
            () -> engine.executeAbility(hero, hero.getAbilita().get(0), farFoe, map));
    }

    @Test
    void executeAbilityRejectsRangedOutOfRange() {
        Unit farFoe = new Unit("FarFoe", UnitClass.WARRIOR, Team.ENEMY, new Position(5, 5));
        map.placeUnit(farFoe, farFoe.getPosizione());
        assertThrows(IllegalArgumentException.class,
            () -> engine.executeAbility(hero, hero.getAbilita().get(1), farFoe, map)); // Arrow range=3
    }

    // ── fireTurnChanged ──────────────────────────────────────────────────────

    @Test
    void fireTurnChangedNotifiesAllListeners() {
        engine.fireTurnChanged(Team.ENEMY, 2);
        assertTrue(listener.events.contains("turn:ENEMY:2"));
    }

    @Test
    void removeListenerStopsNotifications() {
        engine.removeListener(listener);
        engine.executeMove(hero, new Position(1, 0), map);
        assertTrue(listener.events.isEmpty());
    }
}
