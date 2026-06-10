package it.unicam.cs.mpgc.rpg125627.view;

import it.unicam.cs.mpgc.rpg125627.model.Ability;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Pannello laterale che mostra nome, classe, HP (con ProgressBar) e abilità
 * dell'unità correntemente selezionata. Le abilità sono pulsanti cliccabili
 * che invocano il callback {@code onAbilitySelected}.
 */
public class UnitInfoPanel extends VBox {

    private final Label phaseLabel;
    private final Label nameLabel;
    private final Label classLabel;
    private final Label hpLabel;
    private final ProgressBar hpBar;
    private final VBox abilitiesBox;

    public UnitInfoPanel() {
        setSpacing(8);
        setPadding(new Insets(14, 12, 14, 12));
        setPrefWidth(230);
        setMinWidth(230);
        setStyle("-fx-background-color: #16213e; -fx-border-color: #0f3460; -fx-border-width: 0 0 0 2;");

        phaseLabel = new Label("Turno Giocatore");
        phaseLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 12; -fx-font-weight: bold;");
        phaseLabel.setWrapText(true);

        Label unitHeader = new Label("UNITÀ SELEZIONATA");
        unitHeader.setStyle("-fx-text-fill: #778ca3; -fx-font-size: 10; -fx-font-weight: bold;");

        nameLabel = new Label("—");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);

        classLabel = new Label("");
        classLabel.setStyle("-fx-text-fill: #a8dadc; -fx-font-size: 12;");

        hpLabel = new Label("");
        hpLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 11;");

        hpBar = new ProgressBar(0);
        hpBar.setPrefWidth(200);
        hpBar.setPrefHeight(13);
        hpBar.setStyle("-fx-accent: #4caf50;");

        Label abilitiesHeader = new Label("ABILITÀ");
        abilitiesHeader.setStyle("-fx-text-fill: #778ca3; -fx-font-size: 10; -fx-font-weight: bold;");

        abilitiesBox = new VBox(5);

        getChildren().addAll(
            phaseLabel,
            new Separator(),
            unitHeader,
            nameLabel,
            classLabel,
            hpLabel,
            hpBar,
            new Separator(),
            abilitiesHeader,
            abilitiesBox
        );
    }

    // ── API pubblica ──────────────────────────────────────────────────────────

    /** Popola il pannello con i dati dell'unità e i pulsanti delle abilità. */
    public void showUnit(Unit unit, Consumer<Ability> onAbilitySelected) {
        nameLabel.setText(unit.getName());
        classLabel.setText("Classe: " + unit.getClasseUnita().name());
        updateHp(unit);

        abilitiesBox.getChildren().clear();
        for (Ability ability : unit.getAbilita()) {
            Button btn = new Button(ability.getName());
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("""
                -fx-background-color: #0f3460;
                -fx-text-fill: white;
                -fx-font-size: 11;
                -fx-cursor: hand;
                -fx-border-color: #1a5276;
                -fx-border-width: 1;
                """);
            btn.setTooltip(new Tooltip(ability.getDescription()));
            btn.setOnAction(e -> onAbilitySelected.accept(ability));
            abilitiesBox.getChildren().add(btn);
        }
    }

    /** Aggiorna solo HP e ProgressBar senza ricostruire i pulsanti abilità. */
    public void updateHp(Unit unit) {
        double ratio = (double) unit.getHp() / unit.getMaxHp();
        hpLabel.setText("HP: " + unit.getHp() + " / " + unit.getMaxHp());
        hpBar.setProgress(ratio);

        if (ratio > 0.6)      hpBar.setStyle("-fx-accent: #4caf50;");
        else if (ratio > 0.3) hpBar.setStyle("-fx-accent: #ff9800;");
        else                  hpBar.setStyle("-fx-accent: #f44336;");
    }

    /** Abilita o disabilita tutti i pulsanti abilità. */
    public void setAbilitiesEnabled(boolean enabled) {
        abilitiesBox.getChildren().forEach(n -> n.setDisable(!enabled));
    }

    /** Svuota il pannello (nessuna unità selezionata). */
    public void clear() {
        nameLabel.setText("—");
        classLabel.setText("");
        hpLabel.setText("");
        hpBar.setProgress(0);
        hpBar.setStyle("-fx-accent: #4caf50;");
        abilitiesBox.getChildren().clear();
    }

    /** Aggiorna la label del turno corrente. */
    public void setPhaseText(String text, boolean isPlayerTurn) {
        phaseLabel.setText(text);
        phaseLabel.setStyle("-fx-text-fill: " + (isPlayerTurn ? "#4caf50" : "#e94560")
            + "; -fx-font-size: 12; -fx-font-weight: bold;");
    }
}
