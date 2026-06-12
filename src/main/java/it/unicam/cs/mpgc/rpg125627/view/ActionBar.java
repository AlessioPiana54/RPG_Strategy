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
        getStyleClass().add("action-bar");

        endTurnButton = new Button("Fine turno");
        endTurnButton.setPrefWidth(120);
        endTurnButton.setPrefHeight(38);
        endTurnButton.getStyleClass().add("btn-end-turn");

        undoButton = new Button("Annulla");
        undoButton.setPrefWidth(120);
        undoButton.setPrefHeight(38);
        undoButton.getStyleClass().add("btn-undo");

        enemyTurnLabel = new Label("Turno nemico...");
        enemyTurnLabel.getStyleClass().add("enemy-turn-label");
        enemyTurnLabel.setVisible(false);
        enemyTurnLabel.setManaged(false);

        VBox buttons = new VBox(8, endTurnButton, undoButton, enemyTurnLabel);
        buttons.setAlignment(Pos.CENTER);
        buttons.setMinWidth(130);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(4);
        logArea.getStyleClass().add("log-area");

        ScrollPane logScroll = new ScrollPane(logArea);
        logScroll.setFitToWidth(true);
        logScroll.setFitToHeight(true);
        logScroll.getStyleClass().add("log-scroll");
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
