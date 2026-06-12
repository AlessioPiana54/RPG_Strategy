package it.unicam.cs.mpgc.rpg125627.view;

import it.unicam.cs.mpgc.rpg125627.model.GamePhase;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Schermata di fine partita, mostrata quando il {@link GameState} raggiunge
 * la fase {@link GamePhase#VICTORY} o {@link GamePhase#DEFEAT}.
 *
 * <p>Mostra: titolo esito, statistiche (turni, nemici sconfitti, alleati persi)
 * e i pulsanti "Rivai" e "Menu principale".</p>
 */
public class EndGameView extends BorderPane {

    private final Button retryButton = new Button("Rivai");
    private final Button menuButton  = new Button("Menu principale");
    private Runnable onRetry;
    private Runnable onMainMenu;

    public EndGameView(GameState gameState) {
        boolean victory = gameState.getFase() == GamePhase.VICTORY;
        getStyleClass().add("end-bg");

        // ── Titolo ─────────────────────────────────────────────────────────────
        Label titleLabel = new Label(victory ? "Vittoria!" : "Sconfitta");
        titleLabel.getStyleClass().add(victory ? "end-title-victory" : "end-title-defeat");

        Label outcomeLabel = new Label(victory
            ? "Tutti i nemici sono stati sconfitti."
            : "Le tue unità sono state eliminate.");
        outcomeLabel.getStyleClass().add("end-outcome");

        VBox titleBox = new VBox(10, titleLabel, outcomeLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(50, 0, 30, 0));

        // ── Statistiche ────────────────────────────────────────────────────────
        long enemyDefeated = gameState.getUnita().stream()
            .filter(u -> u.getTeam() == Team.ENEMY && !u.isAlive()).count();
        long alliesLost = gameState.getUnita().stream()
            .filter(u -> u.getTeam() == Team.PLAYER && !u.isAlive()).count();
        int turns = gameState.getTurnoCorrente();

        Label statsTitle = new Label("Statistiche");
        statsTitle.getStyleClass().add("end-stats-title");

        VBox statsBox = new VBox(8,
            statsTitle,
            buildStat("Turni giocati",   String.valueOf(turns)),
            buildStat("Nemici sconfitti", String.valueOf(enemyDefeated)),
            buildStat("Alleati persi",    String.valueOf(alliesLost))
        );
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setMaxWidth(340);
        statsBox.getStyleClass().add("end-stats-box");

        // ── Pulsanti ───────────────────────────────────────────────────────────
        retryButton.getStyleClass().add("btn-retry");
        menuButton .getStyleClass().add("btn-menu");

        retryButton.setOnAction(e -> { if (onRetry   != null) onRetry.run(); });
        menuButton .setOnAction(e -> { if (onMainMenu != null) onMainMenu.run(); });

        HBox btnBox = new HBox(16, retryButton, menuButton);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(30, 0, 0, 0));

        // ── Layout ─────────────────────────────────────────────────────────────
        HBox statsRow = new HBox(statsBox);
        statsRow.setAlignment(Pos.CENTER);

        Separator sep = new Separator();
        sep.setMaxWidth(340);

        VBox center = new VBox(20, statsRow, sep, btnBox);
        center.setAlignment(Pos.CENTER);

        VBox root = new VBox(titleBox, center);
        root.setAlignment(Pos.TOP_CENTER);
        setCenter(root);
        setPadding(new Insets(0));
    }

    /** Registra il gestore del pulsante "Rivai" (ricarica la stessa mappa). */
    public void setOnRetry(Runnable handler)    { this.onRetry    = handler; }

    /** Registra il gestore del pulsante "Menu principale". */
    public void setOnMainMenu(Runnable handler) { this.onMainMenu = handler; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static HBox buildStat(String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.getStyleClass().add("stat-label");
        lbl.setMinWidth(180);
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        return new HBox(8, lbl, val);
    }
}
