package it.unicam.cs.mpgc.rpg125627;

import it.unicam.cs.mpgc.rpg125627.controller.BattleEngine;
import it.unicam.cs.mpgc.rpg125627.controller.DefaultGameController;
import it.unicam.cs.mpgc.rpg125627.controller.GameEventListener;
import it.unicam.cs.mpgc.rpg125627.controller.SimpleAIStrategy;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import it.unicam.cs.mpgc.rpg125627.persistence.MapLoadException;
import it.unicam.cs.mpgc.rpg125627.persistence.MapLoader;
import it.unicam.cs.mpgc.rpg125627.persistence.SaveManager;
import it.unicam.cs.mpgc.rpg125627.persistence.XmlGameRepository;
import it.unicam.cs.mpgc.rpg125627.view.ActionBar;
import it.unicam.cs.mpgc.rpg125627.view.EndGameView;
import it.unicam.cs.mpgc.rpg125627.view.GameViewController;
import it.unicam.cs.mpgc.rpg125627.view.LoadDialog;
import it.unicam.cs.mpgc.rpg125627.view.MainView;
import it.unicam.cs.mpgc.rpg125627.view.MapSelectionView;
import it.unicam.cs.mpgc.rpg125627.view.MapView;
import it.unicam.cs.mpgc.rpg125627.view.UnitInfoPanel;
import jakarta.xml.bind.JAXBException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.List;

/**
 * Punto di ingresso dell'applicazione.
 *
 * <p>Flusso schermate:</p>
 * <ol>
 *   <li>Selezione mappa ({@link MapSelectionView})</li>
 *   <li>Partita ({@link MainView})</li>
 *   <li>Fine partita ({@link EndGameView}) con possibilità di ripartire o tornare al menu</li>
 * </ol>
 */
public class Main extends Application {

    private Stage primaryStage;
    private final XmlGameRepository repository = new XmlGameRepository();
    private final MapLoader mapLoader           = new MapLoader();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Tactical RPG");
        stage.setResizable(false);
        showMapSelectionScreen();
        stage.show();
    }

    // ── Schermate ─────────────────────────────────────────────────────────────

    private void showMapSelectionScreen() {
        List<String> maps = mapLoader.listAvailableMaps();
        MapSelectionView view = new MapSelectionView(maps);
        view.setOnMapSelected(this::startGameFromMap);
        view.setOnLoadGame(this::showLoadDialog);
        primaryStage.setScene(new Scene(view, 1100, 720));
    }

    private void startGameFromMap(String mapName) {
        try {
            GameState state = mapLoader.loadMap(mapName);
            buildAndShowScene(state, mapName);
        } catch (MapLoadException e) {
            showError("Errore caricamento mappa",
                    "Impossibile caricare '" + mapName + "':\n" + e.getMessage());
        }
    }

    private void buildAndShowScene(GameState gameState, String currentMapName) {
        SaveManager saveManager = new SaveManager(repository);

        BattleEngine battleEngine           = new BattleEngine();
        DefaultGameController controller    =
                new DefaultGameController(gameState, battleEngine, new SimpleAIStrategy());
        saveManager.setController(controller);

        MapView mapView         = new MapView(gameState.getMappa());
        UnitInfoPanel unitPanel = new UnitInfoPanel();
        ActionBar actionBar     = new ActionBar();
        MainView mainView       = new MainView(mapView, unitPanel, actionBar);

        new GameViewController(controller, battleEngine, mapView, unitPanel, actionBar);

        // Gestore end-game: controlla dopo ogni sconfitta/cambio turno
        battleEngine.addListener(new GameEventListener() {
            private volatile boolean shown = false;

            @Override public void onUnitMoved(Unit u, Position from, Position to) {}
            @Override public void onUnitAttacked(Unit a, Unit t, int d) {}

            @Override
            public void onUnitDefeated(Unit u) {
                Platform.runLater(() -> checkAndShowEndGame(gameState, currentMapName));
            }

            @Override
            public void onTurnChanged(Team newTeam, int turnNumber) {
                checkAndShowEndGame(gameState, currentMapName);
            }

            private void checkAndShowEndGame(GameState gs, String mapName) {
                if (!gs.isOver() || shown) return;
                shown = true;
                // Rimanda leggermente per lasciare che GameViewController completi i suoi runLater
                Platform.runLater(() -> Platform.runLater(() ->
                        showEndGameScreen(gs, mapName)));
            }
        });

        // ── Gestori menu File ────────────────────────────────────────────────
        mainView.setOnNewGame(this::showMapSelectionScreen);
        mainView.setOnSave(() -> showSaveDialog(saveManager, actionBar));
        mainView.setOnLoad(this::showLoadDialog);

        primaryStage.setScene(new Scene(mainView, 1100, 720));
    }

    private void showEndGameScreen(GameState gameState, String mapName) {
        EndGameView view = new EndGameView(gameState);
        view.setOnRetry(() -> startGameFromMap(mapName));
        view.setOnMainMenu(this::showMapSelectionScreen);
        primaryStage.setScene(new Scene(view, 1100, 720));
    }

    // ── Dialoghi menu File ────────────────────────────────────────────────────

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
                // I salvataggi non hanno un nome mappa associato: ricarica dal menu
                buildAndShowScene(loaded, null);
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
