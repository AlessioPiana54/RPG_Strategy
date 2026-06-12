package it.unicam.cs.mpgc.rpg125627.view;

import it.unicam.cs.mpgc.rpg125627.controller.BattleEngine;
import it.unicam.cs.mpgc.rpg125627.controller.DefaultGameController;
import it.unicam.cs.mpgc.rpg125627.controller.GameEventListener;
import it.unicam.cs.mpgc.rpg125627.model.Ability;
import it.unicam.cs.mpgc.rpg125627.model.ActionState;
import it.unicam.cs.mpgc.rpg125627.model.GamePhase;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.HealAbility;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.RangedAttack;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import javafx.application.Platform;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Collega il layer di view al {@link DefaultGameController} implementando
 * {@link GameEventListener} per aggiornare la GUI in risposta agli eventi di gioco.
 *
 * <p>Tutti gli aggiornamenti alla GUI provenienti da eventi del BattleEngine
 * vengono eseguiti tramite {@link Platform#runLater} per garantire la thread-safety.</p>
 *
 * <p>Macchina a stati di interazione:</p>
 * <ul>
 *   <li>{@code IDLE} → clic su unità alleata → {@code UNIT_SELECTED}</li>
 *   <li>{@code UNIT_SELECTED} → clic su cella raggiungibile → movimento</li>
 *   <li>{@code UNIT_SELECTED} → clic su pulsante abilità → {@code ABILITY_TARGETING}</li>
 *   <li>{@code ABILITY_TARGETING} → clic su bersaglio → uso abilità → {@code UNIT_SELECTED}</li>
 * </ul>
 */
public class GameViewController implements GameEventListener {

    private enum InteractionState { IDLE, UNIT_SELECTED, ABILITY_TARGETING }

    private final DefaultGameController controller;
    private final MapView mapView;
    private final UnitInfoPanel unitInfoPanel;
    private final ActionBar actionBar;

    private InteractionState state = InteractionState.IDLE;
    private Unit selectedUnit;
    private Ability selectedAbility;
    private Set<Position> reachablePositions = Set.of();
    private Set<Position> targetablePositions = Set.of();

    public GameViewController(DefaultGameController controller,
                              BattleEngine battleEngine,
                              MapView mapView,
                              UnitInfoPanel unitInfoPanel,
                              ActionBar actionBar) {
        this.controller    = controller;
        this.mapView       = mapView;
        this.unitInfoPanel = unitInfoPanel;
        this.actionBar     = actionBar;

        battleEngine.addListener(this);
        mapView.setOnCellClicked(this::onCellClicked);
        actionBar.setOnEndTurn(this::onEndTurn);
        actionBar.setOnUndo(this::onUndo);

        controller.startGame();
        Platform.runLater(this::fullRefresh);
    }

    // ── Gestione clic sulla cella ─────────────────────────────────────────────

    private void onCellClicked(Position pos) {
        GameState gs = controller.getGameState();
        if (gs.isOver() || gs.getFase() != GamePhase.PLAYER_TURN) return;

        switch (state) {
            case IDLE              -> handleIdleClick(pos, gs);
            case UNIT_SELECTED     -> handleUnitSelectedClick(pos, gs);
            case ABILITY_TARGETING -> handleAbilityTargetingClick(pos);
        }
    }

    private void handleIdleClick(Position pos, GameState gs) {
        Optional<Unit> unit = gs.getMappa().getUnit(pos);
        if (unit.isEmpty()) return;
        if (unit.get().getTeam() == Team.PLAYER
                && unit.get().getActionState() != ActionState.EXHAUSTED) {
            doSelectUnit(unit.get(), pos);
        } else if (unit.get().getTeam() == Team.ENEMY) {
            unitInfoPanel.showEnemyUnit(unit.get());
        }
    }

    private void handleUnitSelectedClick(Position pos, GameState gs) {
        Optional<Unit> unitOpt = gs.getMappa().getUnit(pos);

        if (unitOpt.isPresent() && unitOpt.get().getTeam() == Team.PLAYER) {
            Unit clicked = unitOpt.get();
            if (clicked.getActionState() == ActionState.EXHAUSTED) {
                doDeselect();
                return;
            }
            if (clicked == selectedUnit) {
                refreshHighlights();
            } else {
                doSelectUnit(clicked, pos);
            }
            return;
        }

        // Movimento: solo se l'unità è READY e la cella è raggiungibile
        if (selectedUnit != null
                && selectedUnit.getActionState() == ActionState.READY
                && reachablePositions.contains(pos)) {
            try {
                controller.moveSelectedUnit(pos);
                reachablePositions = Set.of();
                Platform.runLater(this::refreshHighlights);
            } catch (Exception e) {
                actionBar.appendLog("Movimento non valido: " + e.getMessage());
            }
            return;
        }

        if (unitOpt.isPresent() && unitOpt.get().getTeam() == Team.ENEMY) {
            unitInfoPanel.showEnemyUnit(unitOpt.get());
        } else if (unitOpt.isEmpty()) {
            doDeselect();
        }
    }

    private void handleAbilityTargetingClick(Position pos) {
        if (targetablePositions.contains(pos)) {
            try {
                controller.useAbility(selectedAbility, pos);
                actionBar.appendLog("Abilità usata: " + selectedAbility.getName());
            } catch (Exception e) {
                actionBar.appendLog("Abilità non valida: " + e.getMessage());
            }
        }
        selectedAbility      = null;
        targetablePositions  = Set.of();
        state                = InteractionState.UNIT_SELECTED;
        Platform.runLater(() -> {
            fullRefresh();
            checkAutoEndTurn();
        });
    }

    // ── Selezione unità ───────────────────────────────────────────────────────

    private void doSelectUnit(Unit unit, Position pos) {
        controller.selectUnit(pos);
        Unit confirmed = controller.getSelectedUnit();
        if (confirmed == null) return;

        selectedUnit        = confirmed;
        state               = InteractionState.UNIT_SELECTED;
        reachablePositions  = selectedUnit.getActionState() == ActionState.READY
            ? computeReachable(selectedUnit) : Set.of();
        targetablePositions = Set.of();

        unitInfoPanel.showUnit(selectedUnit, this::onAbilitySelected);
        boolean canAct = selectedUnit.getActionState() != ActionState.EXHAUSTED;
        unitInfoPanel.setAbilitiesEnabled(canAct);
        refreshHighlights();
        actionBar.appendLog("Selezionata: " + selectedUnit.getName()
            + " [" + selectedUnit.getActionState() + "]");
    }

    private void doDeselect() {
        state               = InteractionState.IDLE;
        selectedUnit        = null;
        selectedAbility     = null;
        reachablePositions  = Set.of();
        targetablePositions = Set.of();
        mapView.clearOverlays();
        unitInfoPanel.clear();
    }

    // ── Selezione abilità ─────────────────────────────────────────────────────

    private void onAbilitySelected(Ability ability) {
        if (selectedUnit == null || !selectedUnit.isAlive()) return;
        if (selectedUnit.getActionState() == ActionState.EXHAUSTED) return;

        state               = InteractionState.ABILITY_TARGETING;
        selectedAbility     = ability;
        targetablePositions = computeTargetable(selectedUnit, ability);

        mapView.clearOverlays();
        mapView.highlightSelected(selectedUnit.getPosizione());
        if (ability instanceof HealAbility) {
            mapView.showHealTargetable(targetablePositions);
        } else {
            mapView.showTargetable(targetablePositions);
        }
        actionBar.appendLog("Scegli bersaglio per: " + ability.getName());
    }

    // ── Auto fine turno ───────────────────────────────────────────────────────

    /** Chiama automaticamente endTurn() se tutte le unità PLAYER sono EXHAUSTED. */
    private void checkAutoEndTurn() {
        if (!controller.getGameState().isOver()
                && controller.getGameState().getFase() == GamePhase.PLAYER_TURN
                && controller.isEndTurnAvailable()) {
            actionBar.appendLog("─── Tutte le unità hanno agito: fine turno automatico ───");
            doDeselect();
            controller.endTurn();
            Platform.runLater(this::fullRefresh);
        }
    }

    // ── Fine turno / Annulla ──────────────────────────────────────────────────

    private void onEndTurn() {
        if (controller.getGameState().isOver()) return;
        doDeselect();
        actionBar.appendLog("─── Fine turno giocatore ───");
        controller.endTurn();
        Platform.runLater(this::fullRefresh);
    }

    private void onUndo() {
        boolean undone = controller.undoLastCommand();
        if (!undone) {
            actionBar.appendLog("Nessun comando da annullare.");
            return;
        }
        if (selectedUnit != null) {
            reachablePositions = computeReachable(selectedUnit);
        }
        actionBar.appendLog("Ultimo movimento annullato.");
        Platform.runLater(this::fullRefresh);
    }

    // ── Aggiornamento GUI ─────────────────────────────────────────────────────

    private void refreshHighlights() {
        mapView.clearOverlays();
        if (state == InteractionState.UNIT_SELECTED && selectedUnit != null) {
            mapView.highlightSelected(selectedUnit.getPosizione());
            if (selectedUnit.getActionState() == ActionState.READY) {
                mapView.showReachable(reachablePositions);
            }
            boolean canAct = selectedUnit.getActionState() != ActionState.EXHAUSTED;
            unitInfoPanel.setAbilitiesEnabled(canAct);

        } else if (state == InteractionState.ABILITY_TARGETING && selectedUnit != null) {
            mapView.highlightSelected(selectedUnit.getPosizione());
            if (selectedAbility instanceof HealAbility) {
                mapView.showHealTargetable(targetablePositions);
            } else {
                mapView.showTargetable(targetablePositions);
            }
        }
    }

    /** Aggiornamento completo: mappa + highlight + pannello unità + controllo game over. */
    private void fullRefresh() {
        mapView.refresh();
        refreshHighlights();

        if (selectedUnit != null) {
            if (selectedUnit.isAlive()) {
                unitInfoPanel.showUnit(selectedUnit, this::onAbilitySelected);
                boolean canAct = selectedUnit.getActionState() != ActionState.EXHAUSTED;
                unitInfoPanel.setAbilitiesEnabled(canAct);
            } else {
                doDeselect();
            }
        }

        GameState gs = controller.getGameState();
        if (gs.isOver()) {
            String msg = gs.getFase() == GamePhase.VICTORY ? "VITTORIA!" : "SCONFITTA!";
            actionBar.appendLog("═══ " + msg + " ═══");
            actionBar.setEndTurnEnabled(false);
            actionBar.setUndoEnabled(false);
            actionBar.setEnemyTurnActive(false);
        }
    }

    // ── Calcolo posizioni raggiungibili / bersagliabili ───────────────────────

    /** BFS dalla posizione dell'unità fino a moveRange passi su celle percorribili. */
    private Set<Position> computeReachable(Unit unit) {
        Set<Position> reachable = new HashSet<>();
        Map<Position, Integer> dist = new HashMap<>();
        Queue<Position> queue = new LinkedList<>();

        Position start = unit.getPosizione();
        int range = unit.getClasseUnita().getMoveRange();
        GridMap map = controller.getGameState().getMappa();

        dist.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Position curr = queue.poll();
            int d = dist.get(curr);
            if (d >= range) continue;
            for (Position next : map.getAdjacentPositions(curr)) {
                if (dist.containsKey(next) || !map.isWalkable(next)) continue;
                dist.put(next, d + 1);
                reachable.add(next);
                queue.add(next);
            }
        }
        return Collections.unmodifiableSet(reachable);
    }

    /** Raccoglie le posizioni dei bersagli validi per l'abilità selezionata. */
    private Set<Position> computeTargetable(Unit unit, Ability ability) {
        Set<Position> targets = new HashSet<>();
        GameState gs = controller.getGameState();

        final int range;
        final boolean friendlyOnly;
        if (ability instanceof RangedAttack r) {
            range = r.getGittata();
            friendlyOnly = false;
        } else if (ability instanceof HealAbility) {
            range = Integer.MAX_VALUE;
            friendlyOnly = true;
        } else {
            range = 1;
            friendlyOnly = false;
        }

        for (Unit target : gs.getUnita()) {
            if (!target.isAlive()) continue;
            boolean sameTeam = target.getTeam() == unit.getTeam();
            if (friendlyOnly  && !sameTeam) continue;
            if (!friendlyOnly &&  sameTeam) continue;
            if (unit.getPosizione().distanceTo(target.getPosizione()) <= range) {
                targets.add(target.getPosizione());
            }
        }
        return Collections.unmodifiableSet(targets);
    }

    // ── GameEventListener ─────────────────────────────────────────────────────

    @Override
    public void onUnitMoved(Unit unit, Position from, Position to) {
        Platform.runLater(() -> {
            mapView.refresh();
            actionBar.appendLog(unit.getName() + " si sposta: " + from + " → " + to);
        });
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit target, int damage) {
        Platform.runLater(() -> {
            mapView.refresh();
            actionBar.appendLog(attacker.getName() + " attacca " + target.getName()
                + " per " + damage + " danni  [HP " + target.getHp() + "/" + target.getMaxHp() + "]");
            if (selectedUnit != null && selectedUnit == target) {
                unitInfoPanel.updateHp(selectedUnit);
            }
        });
    }

    @Override
    public void onUnitDefeated(Unit unit) {
        Platform.runLater(() -> {
            mapView.refresh();
            actionBar.appendLog("★ " + unit.getName() + " è stato sconfitto!");
            if (selectedUnit == unit) doDeselect();
        });
    }

    @Override
    public void onTurnChanged(Team newTeam, int turnNumber) {
        Platform.runLater(() -> {
            boolean isPlayer = newTeam == Team.PLAYER;
            String txt = "Turno " + turnNumber + " — " + (isPlayer ? "GIOCATORE" : "NEMICO");
            unitInfoPanel.setPhaseText(txt, isPlayer);
            actionBar.appendLog("─── " + txt + " ───");

            // Abilita/disabilita i controlli in base al team attivo
            actionBar.setEnemyTurnActive(!isPlayer);
            if (isPlayer) {
                // Riabilita Fine turno solo se partita ancora in corso
                GameState gs = controller.getGameState();
                if (!gs.isOver()) {
                    actionBar.setEndTurnEnabled(true);
                }
            }
        });
    }
}
