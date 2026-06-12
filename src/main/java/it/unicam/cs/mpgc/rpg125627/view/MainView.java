package it.unicam.cs.mpgc.rpg125627.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

/**
 * Layout radice dell'applicazione (BorderPane):
 * <ul>
 *   <li>Alto   – {@link MenuBar} con il menu File</li>
 *   <li>Centro – {@link MapView} in uno ScrollPane</li>
 *   <li>Destra – {@link UnitInfoPanel}</li>
 *   <li>Basso  – {@link ActionBar}</li>
 * </ul>
 * Gli stili del menu sono definiti in {@code styles.css}.
 */
public class MainView extends BorderPane {

    private final MapView mapView;
    private final UnitInfoPanel unitInfoPanel;
    private final ActionBar actionBar;

    private final MenuItem newGameItem = new MenuItem("Nuova partita");
    private final MenuItem saveItem    = new MenuItem("Salva");
    private final MenuItem loadItem    = new MenuItem("Carica");

    public MainView(MapView mapView, UnitInfoPanel unitInfoPanel, ActionBar actionBar) {
        this.mapView       = mapView;
        this.unitInfoPanel = unitInfoPanel;
        this.actionBar     = actionBar;

        getStyleClass().add("bg-app");
        setPadding(new Insets(0));

        // ── Menu bar ─────────────────────────────────────────────────────────
        MenuItem exitItem = new MenuItem("Esci");
        exitItem.setOnAction(e -> Platform.exit());

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
            newGameItem, saveItem, loadItem,
            new SeparatorMenuItem(), exitItem
        );

        MenuBar menuBar = new MenuBar(fileMenu);
        setTop(menuBar);

        // ── Mappa scrollabile ─────────────────────────────────────────────────
        ScrollPane mapScroll = new ScrollPane(mapView);
        mapScroll.setFitToHeight(false);
        mapScroll.setFitToWidth(false);
        mapScroll.setPannable(true);
        mapScroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        BorderPane inner = new BorderPane();
        inner.setPadding(new Insets(8));
        inner.getStyleClass().add("bg-app");
        inner.setCenter(mapScroll);
        inner.setRight(unitInfoPanel);
        inner.setBottom(actionBar);
        BorderPane.setMargin(unitInfoPanel, new Insets(0, 0, 0, 8));
        BorderPane.setMargin(actionBar,     new Insets(8, 0, 0, 0));

        setCenter(inner);
    }

    // ── Setter per i gestori del menu ─────────────────────────────────────────

    /** Registra il gestore per "Nuova partita". */
    public void setOnNewGame(Runnable handler) {
        newGameItem.setOnAction(e -> handler.run());
    }

    /** Registra il gestore per "Salva". */
    public void setOnSave(Runnable handler) {
        saveItem.setOnAction(e -> handler.run());
    }

    /** Registra il gestore per "Carica". */
    public void setOnLoad(Runnable handler) {
        loadItem.setOnAction(e -> handler.run());
    }

    // ── Accessori ─────────────────────────────────────────────────────────────

    public MapView getMapView()             { return mapView; }
    public UnitInfoPanel getUnitInfoPanel() { return unitInfoPanel; }
    public ActionBar getActionBar()         { return actionBar; }
}
