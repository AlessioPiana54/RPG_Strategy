package it.unicam.cs.mpgc.rpg125627.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Barra inferiore con i pulsanti di azione ("Fine turno", "Annulla"), un'etichetta
 * "Turno nemico..." visibile durante {@code ENEMY_TURN}, e un log testuale scorrevole
 * degli eventi di battaglia.
 */
public class ActionBar extends HBox {

    private final Button endTurnButton;
    private final Button undoButton;
    private final Label  enemyTurnLabel;
    private final TextArea logArea;

    public ActionBar() {
        setSpacing(14);
        setPadding(new Insets(10, 14, 10, 14));
        setPrefHeight(140);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #0d0d1a; -fx-border-color: #1a1a3e; -fx-border-width: 2 0 0 0;");

        endTurnButton = new Button("Fine turno");
        endTurnButton.setPrefWidth(120);
        endTurnButton.setPrefHeight(38);
        endTurnButton.setStyle("""
            -fx-background-color: #e94560;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 13;
            -fx-cursor: hand;
            -fx-background-radius: 4;
            """);

        undoButton = new Button("Annulla");
        undoButton.setPrefWidth(120);
        undoButton.setPrefHeight(38);
        undoButton.setStyle("""
            -fx-background-color: #444;
            -fx-text-fill: #ccc;
            -fx-font-size: 12;
            -fx-cursor: hand;
            -fx-background-radius: 4;
            """);

        enemyTurnLabel = new Label("Turno nemico...");
        enemyTurnLabel.setStyle("""
            -fx-text-fill: #ff6b6b;
            -fx-font-size: 13;
            -fx-font-weight: bold;
            -fx-font-style: italic;
            """);
        enemyTurnLabel.setVisible(false);
        enemyTurnLabel.setManaged(false);

        VBox buttons = new VBox(8, endTurnButton, undoButton, enemyTurnLabel);
        buttons.setAlignment(Pos.CENTER);
        buttons.setMinWidth(130);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(4);
        logArea.setStyle("""
            -fx-background-color: #111;
            -fx-control-inner-background: #111;
            -fx-text-fill: #ccc;
            -fx-font-size: 11;
            -fx-font-family: 'Courier New', monospace;
            """);

        ScrollPane logScroll = new ScrollPane(logArea);
        logScroll.setFitToWidth(true);
        logScroll.setFitToHeight(true);
        logScroll.setStyle("-fx-background: #111; -fx-border-color: #333; -fx-border-width: 1;");
        HBox.setHgrow(logScroll, Priority.ALWAYS);

        getChildren().addAll(buttons, logScroll);
    }

    // ── API pubblica ──────────────────────────────────────────────────────────

    public void setOnEndTurn(Runnable handler) {
        endTurnButton.setOnAction(e -> handler.run());
    }

    public void setOnUndo(Runnable handler) {
        undoButton.setOnAction(e -> handler.run());
    }

    /** Aggiunge una riga al log e scorre automaticamente in fondo. */
    public void appendLog(String message) {
        logArea.appendText(message + "\n");
    }

    public void setEndTurnEnabled(boolean enabled) {
        endTurnButton.setDisable(!enabled);
    }

    public void setUndoEnabled(boolean enabled) {
        undoButton.setDisable(!enabled);
    }

    /**
     * Mostra o nasconde l'etichetta "Turno nemico..." e disabilita/abilita
     * i pulsanti di conseguenza.
     */
    public void setEnemyTurnActive(boolean active) {
        enemyTurnLabel.setVisible(active);
        enemyTurnLabel.setManaged(active);
        endTurnButton.setDisable(active);
        undoButton.setDisable(active);
    }
}
