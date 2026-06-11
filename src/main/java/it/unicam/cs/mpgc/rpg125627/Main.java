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
import it.unicam.cs.mpgc.rpg125627.persistence.SaveManager;
import it.unicam.cs.mpgc.rpg125627.persistence.XmlGameRepository;
import it.unicam.cs.mpgc.rpg125627.view.ActionBar;
import it.unicam.cs.mpgc.rpg125627.view.GameViewController;
import it.unicam.cs.mpgc.rpg125627.view.LoadDialog;
import it.unicam.cs.mpgc.rpg125627.view.MainView;
import it.unicam.cs.mpgc.rpg125627.view.MapView;
import it.unicam.cs.mpgc.rpg125627.view.UnitInfoPanel;
import jakarta.xml.bind.JAXBException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.List;

/**
 * Punto di ingresso. Costruisce lo scenario di battaglia, crea le view
 * e le collega al controller tramite {@link GameViewController}.
 * Gestisce anche i comandi del menu File (nuova partita, salva, carica).
 */
public class Main extends Application {

    private Stage primaryStage;
    private final XmlGameRepository repository = new XmlGameRepository();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        buildAndShowScene(createInitialGameState());
    }

    // ── Costruzione della scena ───────────────────────────────────────────────

    /**
     * (Re)costruisce l'intera scena di gioco a partire da un {@link GameState}.
     * Chiamato sia all'avvio sia dopo "Nuova partita" o "Carica".
     */
    private void buildAndShowScene(GameState gameState) {
        SaveManager saveManager = new SaveManager(repository);

        BattleEngine battleEngine = new BattleEngine();
        DefaultGameController controller =
                new DefaultGameController(gameState, battleEngine, new SimpleAIStrategy());
        saveManager.setController(controller);

        MapView mapView         = new MapView(gameState.getMappa());
        UnitInfoPanel unitPanel = new UnitInfoPanel();
        ActionBar actionBar     = new ActionBar();
        MainView mainView       = new MainView(mapView, unitPanel, actionBar);

        new GameViewController(controller, battleEngine, mapView, unitPanel, actionBar);

        // ── Gestori del menu File ─────────────────────────────────────────────
        mainView.setOnNewGame(() -> buildAndShowScene(createInitialGameState()));
        mainView.setOnSave(() -> showSaveDialog(saveManager, actionBar));
        mainView.setOnLoad(() -> showLoadDialog());

        Scene scene = new Scene(mainView, 1100, 720);
        primaryStage.setTitle("Tactical RPG");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    // ── Stato iniziale di gioco ───────────────────────────────────────────────

    /** Crea lo scenario di battaglia predefinito (mappa 8×10, 3 vs 3). */
    private GameState createInitialGameState() {
        GridMap map = new GridMap(8, 10);

        map.setTileType(new Position(2, 4), TileType.MOUNTAIN);
        map.setTileType(new Position(3, 4), TileType.MOUNTAIN);
        map.setTileType(new Position(3, 5), TileType.MOUNTAIN);
        map.setTileType(new Position(1, 3), TileType.FOREST);
        map.setTileType(new Position(2, 7), TileType.FOREST);
        map.setTileType(new Position(5, 2), TileType.FOREST);
        map.setTileType(new Position(6, 6), TileType.FOREST);

        Unit warrior = new Unit("Guerriero", UnitClass.WARRIOR, Team.PLAYER, new Position(1, 0));
        warrior.addAbilita(new MeleeAttack("Colpo di Spada", 35));
        warrior.addAbilita(new MeleeAttack("Scudo Frantumato", 20));

        Unit mage = new Unit("Mago", UnitClass.MAGE, Team.PLAYER, new Position(4, 0));
        mage.addAbilita(new RangedAttack("Sfera di Fuoco", 45, 4));
        mage.addAbilita(new HealAbility("Guarigione", 30));

        Unit archer = new Unit("Arciere", UnitClass.ARCHER, Team.PLAYER, new Position(6, 1));
        archer.addAbilita(new RangedAttack("Tiro con l'Arco", 30, 3));
        archer.addAbilita(new MeleeAttack("Colpo Ravvicinato", 15));

        Unit brutus = new Unit("Brutus", UnitClass.WARRIOR, Team.ENEMY, new Position(1, 9));
        brutus.addAbilita(new MeleeAttack("Colpo Brutale", 30));
        brutus.addAbilita(new MeleeAttack("Spinta", 15));

        Unit strega = new Unit("Strega", UnitClass.MAGE, Team.ENEMY, new Position(4, 9));
        strega.addAbilita(new RangedAttack("Saetta Oscura", 40, 4));

        Unit cecchino = new Unit("Cecchino", UnitClass.ARCHER, Team.ENEMY, new Position(6, 8));
        cecchino.addAbilita(new RangedAttack("Freccia Precisa", 28, 3));

        List<Unit> allUnits = List.of(warrior, mage, archer, brutus, strega, cecchino);
        for (Unit u : allUnits) {
            map.placeUnit(u, u.getPosizione());
        }
        return new GameState(map, allUnits);
    }

    // ── Dialoghi del menu File ────────────────────────────────────────────────

    private void showSaveDialog(SaveManager saveManager, ActionBar actionBar) {
        TextInputDialog dialog = new TextInputDialog("partita1");
        dialog.initOwner(primaryStage);
        dialog.setTitle("Salva partita");
        dialog.setHeaderText("Inserisci il nome del salvataggio:");
        dialog.setContentText("Nome:");

        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (trimmed.isBlank()) return;
            try {
                saveManager.save(trimmed);
                actionBar.appendLog("Partita salvata: " + trimmed);
            } catch (JAXBException e) {
                actionBar.appendLog("Errore nel salvataggio: " + e.getMessage());
                showError("Errore salvataggio",
                        "Impossibile salvare la partita:\n" + e.getMessage());
            }
        });
    }

    private void showLoadDialog() {
        var entries = repository.listSaveEntries();
        if (entries.isEmpty()) {
            Alert info = new Alert(Alert.AlertType.INFORMATION,
                    "Nessun salvataggio trovato.", ButtonType.OK);
            info.initOwner(primaryStage);
            info.setTitle("Carica partita");
            info.showAndWait();
            return;
        }

        LoadDialog dialog = new LoadDialog(primaryStage, entries);
        dialog.showAndWait().ifPresent(name -> {
            try {
                GameState loaded = repository.load(name);
                buildAndShowScene(loaded);
            } catch (JAXBException e) {
                showError("Errore caricamento",
                        "Impossibile caricare '" + name + "':\n" + e.getMessage());
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
