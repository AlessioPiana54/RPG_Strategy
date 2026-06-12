package it.unicam.cs.mpgc.rpg125627.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Schermata iniziale di selezione mappa.
 *
 * <p>Mostra l'elenco delle mappe disponibili lette da {@code resources/maps/}
 * tramite {@link it.unicam.cs.mpgc.rpg125627.persistence.XmlMapRepository#listAvailableMaps()}.
 * L'utente seleziona una voce e preme "Inizia partita".</p>
 */
public class MapSelectionView extends BorderPane {

    private final ListView<String> mapList    = new ListView<>();
    private final Button startButton          = new Button("Inizia partita");
    private final Button loadButton           = new Button("Carica partita");
    private Consumer<String> onMapSelected;
    private Runnable onLoadGame;

    public MapSelectionView(List<String> availableMaps) {
        getStyleClass().add("bg-app");

        // ── Titolo ────────────────────────────────────────────────────────────
        Label title = new Label("Tactical RPG");
        title.getStyleClass().add("map-title");

        Label subtitle = new Label("Seleziona una mappa");
        subtitle.getStyleClass().add("map-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 0, 30, 0));

        // ── Lista mappe ───────────────────────────────────────────────────────
        mapList.getItems().addAll(availableMaps);
        mapList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        mapList.getStyleClass().add("map-list");
        mapList.setMaxWidth(400);
        mapList.setPrefHeight(180);

        if (!availableMaps.isEmpty()) {
            mapList.getSelectionModel().selectFirst();
        }

        // Doppio clic avvia la partita direttamente
        mapList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) fireStart();
        });

        // ── Pulsanti ─────────────────────────────────────────────────────────
        startButton.getStyleClass().add("btn-start");
        startButton.setOnAction(e -> fireStart());
        startButton.setDisable(availableMaps.isEmpty());

        loadButton.getStyleClass().add("btn-load");
        loadButton.setOnAction(e -> { if (onLoadGame != null) onLoadGame.run(); });

        // ── Messaggio assenza mappe ───────────────────────────────────────────
        Label noMaps = new Label(availableMaps.isEmpty()
            ? "Nessuna mappa trovata in resources/maps/" : "");
        noMaps.getStyleClass().add("no-maps-label");

        // ── Layout centrale ───────────────────────────────────────────────────
        HBox listBox = new HBox(mapList);
        listBox.setAlignment(Pos.CENTER);

        HBox btnBox = new HBox(16, startButton, loadButton);
        btnBox.setAlignment(Pos.CENTER);

        HBox msgBox = new HBox(noMaps);
        msgBox.setAlignment(Pos.CENTER);

        VBox center = new VBox(16, listBox, btnBox, msgBox);
        center.setAlignment(Pos.CENTER);

        VBox root = new VBox(header, center);
        root.setAlignment(Pos.TOP_CENTER);
        root.setFillWidth(true);

        setCenter(root);
        setPadding(new Insets(0));
    }

    /** Registra il gestore invocato quando l'utente avvia la partita. */
    public void setOnMapSelected(Consumer<String> handler) {
        this.onMapSelected = handler;
    }

    /** Registra il gestore invocato quando l'utente preme "Carica partita". */
    public void setOnLoadGame(Runnable handler) {
        this.onLoadGame = handler;
    }

    private void fireStart() {
        String selected = mapList.getSelectionModel().getSelectedItem();
        if (selected != null && onMapSelected != null) {
            onMapSelected.accept(selected);
        }
    }
}
