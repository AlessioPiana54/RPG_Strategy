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
 * tramite {@link it.unicam.cs.mpgc.rpg125627.persistence.MapLoader#listAvailableMaps()}.
 * L'utente seleziona una voce e preme "Inizia partita".</p>
 */
public class MapSelectionView extends BorderPane {

    private final ListView<String> mapList = new ListView<>();
    private final Button startButton = new Button("Inizia partita");
    private Consumer<String> onMapSelected;

    public MapSelectionView(List<String> availableMaps) {
        setStyle("-fx-background-color: #1a1a2e;");

        // ── Titolo ────────────────────────────────────────────────────────────
        Label title = new Label("Tactical RPG");
        title.setStyle("-fx-text-fill: #e0e0ff; -fx-font-size: 36px; " +
                       "-fx-font-weight: bold; -fx-font-family: 'Monospace';");

        Label subtitle = new Label("Seleziona una mappa");
        subtitle.setStyle("-fx-text-fill: #a0a0cc; -fx-font-size: 16px; " +
                          "-fx-font-family: 'Monospace';");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 0, 30, 0));

        // ── Lista mappe ───────────────────────────────────────────────────────
        mapList.getItems().addAll(availableMaps);
        mapList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        mapList.setStyle("""
                -fx-background-color: #0d0d1a;
                -fx-border-color: #3a3a6e;
                -fx-border-width: 1;
                -fx-font-family: 'Monospace';
                -fx-font-size: 14px;
                -fx-control-inner-background: #0d0d1a;
                -fx-text-fill: #c0c0ee;
                """);
        mapList.setMaxWidth(400);
        mapList.setPrefHeight(180);

        if (!availableMaps.isEmpty()) {
            mapList.getSelectionModel().selectFirst();
        }

        // Doppio clic avvia la partita direttamente
        mapList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) fireStart();
        });

        // ── Pulsante ──────────────────────────────────────────────────────────
        startButton.setStyle("""
                -fx-background-color: #3a5aad;
                -fx-text-fill: white;
                -fx-font-size: 15px;
                -fx-font-family: 'Monospace';
                -fx-padding: 10 32 10 32;
                -fx-cursor: hand;
                """);
        startButton.setOnMouseEntered(e ->
                startButton.setStyle(startButton.getStyle()
                        .replace("#3a5aad", "#5070cd")));
        startButton.setOnMouseExited(e ->
                startButton.setStyle(startButton.getStyle()
                        .replace("#5070cd", "#3a5aad")));
        startButton.setOnAction(e -> fireStart());
        startButton.setDisable(availableMaps.isEmpty());

        // ── Messaggio assenza mappe ───────────────────────────────────────────
        Label noMaps = new Label(availableMaps.isEmpty()
                ? "Nessuna mappa trovata in resources/maps/" : "");
        noMaps.setStyle("-fx-text-fill: #cc4444; -fx-font-family: 'Monospace'; -fx-font-size: 13px;");

        // ── Layout centrale ───────────────────────────────────────────────────
        HBox listBox = new HBox(mapList);
        listBox.setAlignment(Pos.CENTER);

        HBox btnBox = new HBox(startButton);
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

    private void fireStart() {
        String selected = mapList.getSelectionModel().getSelectedItem();
        if (selected != null && onMapSelected != null) {
            onMapSelected.accept(selected);
        }
    }
}
