package it.unicam.cs.mpgc.rpg125627.model;

import it.unicam.cs.mpgc.rpg125627.persistence.XmlMap;
import it.unicam.cs.mpgc.rpg125627.persistence.XmlTile;
import it.unicam.cs.mpgc.rpg125627.persistence.XmlUnitSpawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Costruisce un {@link GameState} iniziale a partire da un {@link XmlMap} già deserializzato.
 *
 * <p>Statistiche bilanciate (obiettivo: partita media di 8-12 turni con 3 unità per parte):</p>
 * <ul>
 *   <li>WARRIOR — HP 120, moveRange 3, attackRange 1, danno 30</li>
 *   <li>MAGE    — HP  80, moveRange 2, attackRange 3, danno 45</li>
 *   <li>ARCHER  — HP  90, moveRange 3, attackRange 2, danno 35</li>
 * </ul>
 */
public final class ScenarioFactory {

    private ScenarioFactory() {}

    /**
     * Costruisce il {@link GameState} iniziale dal descrittore XML della mappa.
     *
     * @param xmlMap mappa deserializzata da file XML
     * @return stato di gioco pronto all'avvio
     * @throws IllegalArgumentException se {@code unitClass} o {@code team} non sono valori enum validi
     */
    public static GameState build(XmlMap xmlMap) {
        GridMap map = new GridMap(xmlMap.rows, xmlMap.cols);

        if (xmlMap.tiles != null) {
            for (XmlTile xt : xmlMap.tiles) {
                map.setTileType(new Position(xt.row, xt.col), TileType.valueOf(xt.tileType));
            }
        }

        // Contatori per generare nomi univoci quando ci sono più unità della stessa classe/team
        Map<String, AtomicInteger> counters = new java.util.HashMap<>();
        List<Unit> units = new ArrayList<>();

        if (xmlMap.spawns != null) {
            for (XmlUnitSpawn spawn : xmlMap.spawns) {
                UnitClass uc   = UnitClass.valueOf(spawn.unitClass);
                Team      team = Team.valueOf(spawn.team);
                String    name = buildName(uc, team, counters);
                Unit      unit = new Unit(name, uc, team, new Position(spawn.row, spawn.col));
                addAbilities(unit, uc);
                units.add(unit);
            }
        }

        for (Unit u : units) {
            map.placeUnit(u, u.getPosizione());
        }
        return new GameState(map, units);
    }

    // ── Nomi ─────────────────────────────────────────────────────────────────

    private static final Map<String, String[]> NAMES = Map.of(
            "PLAYER_WARRIOR", new String[]{"Guerriero", "Paladin", "Cavaliere"},
            "PLAYER_MAGE",    new String[]{"Mago", "Stregone", "Veggente"},
            "PLAYER_ARCHER",  new String[]{"Arciere", "Esploratore", "Cecchino"},
            "ENEMY_WARRIOR",  new String[]{"Brutus", "Golem", "Berserker"},
            "ENEMY_MAGE",     new String[]{"Strega", "Necromante", "Ombra"},
            "ENEMY_ARCHER",   new String[]{"Cecchino", "Cacciatore", "Tiratore"}
    );

    private static String buildName(UnitClass uc, Team team,
                                    Map<String, AtomicInteger> counters) {
        String key = team.name() + "_" + uc.name();
        int idx = counters.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement();
        String[] pool = NAMES.getOrDefault(key, new String[]{uc.name()});
        return pool[Math.min(idx, pool.length - 1)];
    }

    // ── Abilità ───────────────────────────────────────────────────────────────

    private static void addAbilities(Unit unit, UnitClass uc) {
        switch (uc) {
            case WARRIOR -> {
                unit.addAbilita(new MeleeAttack("Colpo di Spada", 30));
                unit.addAbilita(new MeleeAttack("Scudo Frantumato", 15));
            }
            case MAGE -> {
                unit.addAbilita(new RangedAttack("Sfera di Fuoco", 45, UnitClass.MAGE.getAttackRange()));
                unit.addAbilita(new HealAbility("Guarigione", 25));
            }
            case ARCHER -> {
                unit.addAbilita(new RangedAttack("Tiro con l'Arco", 35, UnitClass.ARCHER.getAttackRange()));
                unit.addAbilita(new MeleeAttack("Colpo Ravvicinato", 15));
            }
        }
    }
}
