package it.unicam.cs.mpgc.rpg125627;

import it.unicam.cs.mpgc.rpg125627.controller.BattleEngine;
import it.unicam.cs.mpgc.rpg125627.controller.DefaultGameController;
import it.unicam.cs.mpgc.rpg125627.controller.SimpleAIStrategy;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.HealAbility;
import it.unicam.cs.mpgc.rpg125627.model.MeleeAttack;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.RangedAttack;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.TileType;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import it.unicam.cs.mpgc.rpg125627.model.UnitClass;
import it.unicam.cs.mpgc.rpg125627.view.ActionBar;
import it.unicam.cs.mpgc.rpg125627.view.GameViewController;
import it.unicam.cs.mpgc.rpg125627.view.MainView;
import it.unicam.cs.mpgc.rpg125627.view.MapView;
import it.unicam.cs.mpgc.rpg125627.view.UnitInfoPanel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

/**
 * Punto di ingresso. Costruisce lo scenario di battaglia, crea le view
 * e le collega al controller tramite {@link GameViewController}.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        // ── Mappa 8×10 ────────────────────────────────────────────────────────
        GridMap map = new GridMap(8, 10);

        // Montagne (non percorribili)
        map.setTileType(new Position(2, 4), TileType.MOUNTAIN);
        map.setTileType(new Position(3, 4), TileType.MOUNTAIN);
        map.setTileType(new Position(3, 5), TileType.MOUNTAIN);

        // Foreste (percorribili, copertura)
        map.setTileType(new Position(1, 3), TileType.FOREST);
        map.setTileType(new Position(2, 7), TileType.FOREST);
        map.setTileType(new Position(5, 2), TileType.FOREST);
        map.setTileType(new Position(6, 6), TileType.FOREST);

        // ── Unità PLAYER (lato sinistro) ──────────────────────────────────────
        Unit warrior = new Unit("Guerriero", UnitClass.WARRIOR, Team.PLAYER, new Position(1, 0));
        warrior.addAbilita(new MeleeAttack("Colpo di Spada", 35));
        warrior.addAbilita(new MeleeAttack("Scudo Frantumato", 20));

        Unit mage = new Unit("Mago", UnitClass.MAGE, Team.PLAYER, new Position(4, 0));
        mage.addAbilita(new RangedAttack("Sfera di Fuoco", 45, 4));
        mage.addAbilita(new HealAbility("Guarigione", 30));

        Unit archer = new Unit("Arciere", UnitClass.ARCHER, Team.PLAYER, new Position(6, 1));
        archer.addAbilita(new RangedAttack("Tiro con l'Arco", 30, 3));
        archer.addAbilita(new MeleeAttack("Colpo Ravvicinato", 15));

        // ── Unità ENEMY (lato destro) ─────────────────────────────────────────
        Unit brutus = new Unit("Brutus", UnitClass.WARRIOR, Team.ENEMY, new Position(1, 9));
        brutus.addAbilita(new MeleeAttack("Colpo Brutale", 30));
        brutus.addAbilita(new MeleeAttack("Spinta", 15));

        Unit strega = new Unit("Strega", UnitClass.MAGE, Team.ENEMY, new Position(4, 9));
        strega.addAbilita(new RangedAttack("Saetta Oscura", 40, 4));

        Unit cecchino = new Unit("Cecchino", UnitClass.ARCHER, Team.ENEMY, new Position(6, 8));
        cecchino.addAbilita(new RangedAttack("Freccia Precisa", 28, 3));

        // ── Posizionamento sulla mappa ────────────────────────────────────────
        List<Unit> allUnits = List.of(warrior, mage, archer, brutus, strega, cecchino);
        for (Unit u : allUnits) {
            map.placeUnit(u, u.getPosizione());
        }

        // ── Controller ───────────────────────────────────────────────────────
        GameState gameState      = new GameState(map, allUnits);
        BattleEngine battleEngine = new BattleEngine();
        DefaultGameController controller =
            new DefaultGameController(gameState, battleEngine, new SimpleAIStrategy());

        // ── View ──────────────────────────────────────────────────────────────
        MapView mapView           = new MapView(map);
        UnitInfoPanel unitPanel   = new UnitInfoPanel();
        ActionBar actionBar       = new ActionBar();
        MainView mainView         = new MainView(mapView, unitPanel, actionBar);

        // Collega view e controller (registra anche il GameEventListener)
        new GameViewController(controller, battleEngine, mapView, unitPanel, actionBar);

        // ── Finestra ─────────────────────────────────────────────────────────
        Scene scene = new Scene(mainView, 1100, 700);
        primaryStage.setTitle("Tactical RPG");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
