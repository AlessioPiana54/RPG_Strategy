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
        getStyleClass().add("unit-panel");

        phaseLabel = new Label("Turno Giocatore");
        phaseLabel.getStyleClass().addAll("unit-phase", "unit-phase-player");
        phaseLabel.setWrapText(true);

        Label unitHeader = new Label("UNITÀ SELEZIONATA");
        unitHeader.getStyleClass().add("unit-section-header");

        nameLabel = new Label("—");
        nameLabel.getStyleClass().add("unit-name");
        nameLabel.setWrapText(true);

        classLabel = new Label("");
        classLabel.getStyleClass().add("unit-class-label");

        hpLabel = new Label("");
        hpLabel.getStyleClass().add("unit-hp-label");

        hpBar = new ProgressBar(0);
        hpBar.setPrefWidth(200);
        hpBar.setPrefHeight(13);
        hpBar.getStyleClass().add("hp-bar-high");

        Label abilitiesHeader = new Label("ABILITÀ");
        abilitiesHeader.getStyleClass().add("unit-section-header");

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
            btn.getStyleClass().add("ability-btn");
            btn.setTooltip(new Tooltip(ability.getDescription()));
            btn.setOnAction(e -> onAbilitySelected.accept(ability));
            abilitiesBox.getChildren().add(btn);
        }
    }

    /** Aggiorna solo HP e ProgressBar senza ricostruire i pulsanti abilità. */
    public void updateHp(Unit unit) {
        double ratio = unit.getMaxHp() > 0
            ? (double) unit.getHp() / unit.getMaxHp() : 0;
        hpLabel.setText("HP: " + unit.getHp() + " / " + unit.getMaxHp());
        hpBar.setProgress(ratio);

        hpBar.getStyleClass().removeAll("hp-bar-high", "hp-bar-mid", "hp-bar-low");
        if (ratio > 0.6)      hpBar.getStyleClass().add("hp-bar-high");
        else if (ratio > 0.3) hpBar.getStyleClass().add("hp-bar-mid");
        else                  hpBar.getStyleClass().add("hp-bar-low");
    }

    /** Abilita o disabilita tutti i pulsanti abilità. */
    public void setAbilitiesEnabled(boolean enabled) {
        abilitiesBox.getChildren().forEach(n -> n.setDisable(!enabled));
    }

    /** Mostra le info di un'unità nemica in sola lettura (abilità non cliccabili). */
    public void showEnemyUnit(Unit unit) {
        nameLabel.setText(unit.getName());
        classLabel.setText("Classe: " + unit.getClasseUnita().name() + " [NEMICO]");
        updateHp(unit);

        abilitiesBox.getChildren().clear();
        for (Ability ability : unit.getAbilita()) {
            Label lbl = new Label("• " + ability.getName());
            lbl.getStyleClass().add("enemy-ability");
            lbl.setTooltip(new Tooltip(ability.getDescription()));
            abilitiesBox.getChildren().add(lbl);
        }
    }

    /** Svuota il pannello (nessuna unità selezionata). */
    public void clear() {
        nameLabel.setText("—");
        classLabel.setText("");
        hpLabel.setText("");
        hpBar.setProgress(0);
        hpBar.getStyleClass().removeAll("hp-bar-high", "hp-bar-mid", "hp-bar-low");
        hpBar.getStyleClass().add("hp-bar-high");
        abilitiesBox.getChildren().clear();
    }

    /** Aggiorna la label del turno corrente. */
    public void setPhaseText(String text, boolean isPlayerTurn) {
        phaseLabel.setText(text);
        phaseLabel.getStyleClass().removeAll("unit-phase-player", "unit-phase-enemy");
        phaseLabel.getStyleClass().add(isPlayerTurn ? "unit-phase-player" : "unit-phase-enemy");
    }
}
