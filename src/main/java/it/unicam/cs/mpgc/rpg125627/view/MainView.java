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
 */
public class MainView extends BorderPane {

    private final MapView mapView;
    private final UnitInfoPanel unitInfoPanel;
    private final ActionBar actionBar;

    private final MenuItem newGameItem   = new MenuItem("Nuova partita");
    private final MenuItem saveItem      = new MenuItem("Salva");
    private final MenuItem loadItem      = new MenuItem("Carica");

    public MainView(MapView mapView, UnitInfoPanel unitInfoPanel, ActionBar actionBar) {
        this.mapView       = mapView;
        this.unitInfoPanel = unitInfoPanel;
        this.actionBar     = actionBar;

        setStyle("-fx-background-color: #1a1a2e;");
        setPadding(new Insets(0));

        // ── Menu bar ─────────────────────────────────────────────────────────
        MenuItem exitItem = new MenuItem("Esci");
        exitItem.setOnAction(e -> Platform.exit());

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
                newGameItem,
                saveItem,
                loadItem,
                new SeparatorMenuItem(),
                exitItem
        );

        MenuBar menuBar = new MenuBar(fileMenu);
        menuBar.setStyle("-fx-background-color: #0f3460; -fx-border-color: #3a5aad; " +
                         "-fx-border-width: 0 0 2 0; -fx-font-size: 13px; -fx-font-family: 'Monospace';");
        menuBar.getStylesheets().add(
            "data:text/css," +
            ".menu-bar .menu .label{-fx-text-fill:white !important;}" +
            ".menu-bar .menu:hover .label,.menu-bar .menu:showing .label{-fx-text-fill:white !important;}" +
            ".context-menu{-fx-background-color:#0d0d1a;-fx-border-color:#3a5aad;-fx-border-width:1;}" +
            ".menu-item{-fx-background-color:#0d0d1a;}" +
            ".menu-item .label{-fx-text-fill:#3a7bd5 !important;}" +
            ".menu-item:hover,.menu-item:focused{-fx-background-color:#1a1a3e;}" +
            ".menu-item:hover .label,.menu-item:focused .label{-fx-text-fill:#6aa3f5 !important;}");
        setTop(menuBar);

        // ── Mappa scrollabile ─────────────────────────────────────────────────
        ScrollPane mapScroll = new ScrollPane(mapView);
        mapScroll.setFitToHeight(false);
        mapScroll.setFitToWidth(false);
        mapScroll.setPannable(true);
        mapScroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        BorderPane inner = new BorderPane();
        inner.setPadding(new Insets(8));
        inner.setStyle("-fx-background-color: #1a1a2e;");
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
