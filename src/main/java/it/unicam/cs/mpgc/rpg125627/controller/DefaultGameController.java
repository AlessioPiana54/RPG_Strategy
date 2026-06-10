package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Ability;
import it.unicam.cs.mpgc.rpg125627.model.GamePhase;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Implementazione predefinita di {@link GameController}.
 *
 * <p>Responsabilità:</p>
 * <ul>
 *   <li>Selezione delle unità con vincolo di fase/team (solo le unità del team attivo possono essere selezionate)</li>
 *   <li>Validazione di movimenti e abilità delegata a {@link BattleEngine} tramite {@link GameCommand}</li>
 *   <li>Orchestrazione dei turni: turno giocatore → turno IA nemica → turno giocatore, con notifiche agli event listener</li>
 *   <li>Annullamento facoltativo dell'ultimo comando durante il turno del giocatore</li>
 * </ul>
 *
 * <p>Nessuna dipendenza da JavaFX: può essere costruita e testata con semplici test JUnit.</p>
 */
public class DefaultGameController implements GameController {

    private final GameState gameState;
    private final BattleEngine battleEngine;
    private final AIStrategy aiStrategy;
    private final TurnManager turnManager;
    private final Deque<GameCommand> commandHistory = new ArrayDeque<>();

    private Unit selectedUnit;
    private boolean unitHasMoved;
    private boolean unitHasActed;

    public DefaultGameController(GameState gameState,
                                 BattleEngine battleEngine,
                                 AIStrategy aiStrategy) {
        this.gameState    = Objects.requireNonNull(gameState,    "gameState");
        this.battleEngine = Objects.requireNonNull(battleEngine, "battleEngine");
        this.aiStrategy   = Objects.requireNonNull(aiStrategy,   "aiStrategy");
        this.turnManager  = new TurnManager(gameState);
    }

    // ── GameController ───────────────────────────────────────────────────────

    @Override
    public void startGame() {
        turnManager.resetTurn();
        battleEngine.fireTurnChanged(Team.PLAYER, gameState.getTurnoCorrente());
    }

    @Override
    public void selectUnit(Position position) {
        Objects.requireNonNull(position, "position");
        if (gameState.isOver()) return;

        Team expectedTeam = gameState.getFase() == GamePhase.PLAYER_TURN
            ? Team.PLAYER : Team.ENEMY;

        gameState.getMappa().getUnit(position).ifPresent(unit -> {
            if (unit.getTeam() == expectedTeam && unit.isAlive()) {
                selectedUnit  = unit;
                unitHasMoved  = false;
                unitHasActed  = false;
            }
        });
    }

    @Override
    public void moveSelectedUnit(Position target) {
        Objects.requireNonNull(target, "target");
        if (selectedUnit == null || unitHasMoved || !isActiveTurn()) return;

        MoveCommand cmd = new MoveCommand(selectedUnit, target, battleEngine, gameState.getMappa());
        cmd.execute();
        commandHistory.push(cmd);
        unitHasMoved = true;
    }

    @Override
    public void useAbility(Ability ability, Position targetPosition) {
        Objects.requireNonNull(ability,         "ability");
        Objects.requireNonNull(targetPosition,  "targetPosition");
        if (selectedUnit == null || unitHasActed || !isActiveTurn()) return;

        gameState.getMappa().getUnit(targetPosition).ifPresent(target -> {
            AbilityCommand cmd = new AbilityCommand(
                selectedUnit, ability, target, battleEngine, gameState.getMappa());
            cmd.execute();
            commandHistory.push(cmd);
            unitHasActed = true;
            gameState.checkVictory();
        });
    }

    @Override
    public void endTurn() {
        if (gameState.isOver()) return;

        clearSelection();
        commandHistory.clear();

        // Giocatore → Nemico
        gameState.nextTurn();
        battleEngine.fireTurnChanged(Team.ENEMY, gameState.getTurnoCorrente());
        turnManager.resetTurn();

        if (!gameState.isOver()) {
            aiStrategy.playTurn(gameState, this);
        }

        // Nemico → Giocatore (incrementa il contatore dei turni in GameState)
        if (!gameState.isOver()) {
            gameState.nextTurn();
            battleEngine.fireTurnChanged(Team.PLAYER, gameState.getTurnoCorrente());
            turnManager.resetTurn();
            clearSelection();
        }
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    // ── API aggiuntiva ───────────────────────────────────────────────────────

    /**
     * Annulla l'ultimo comando di movimento o abilità eseguito durante il turno corrente
     * del giocatore. Solo i {@link MoveCommand} supportano completamente l'annullamento;
     * gli {@link AbilityCommand} no.
     *
     * @return {@code true} se un comando è stato annullato
     */
    public boolean undoLastCommand() {
        if (commandHistory.isEmpty()) return false;
        GameCommand cmd = commandHistory.pop();
        cmd.undo();
        if (cmd instanceof MoveCommand)    unitHasMoved = false;
        if (cmd instanceof AbilityCommand) unitHasActed = false;
        return true;
    }

    /** Restituisce l'unità attualmente selezionata, o {@code null} se nessuna è selezionata. */
    public Unit getSelectedUnit() {
        return selectedUnit;
    }

    // ── Metodi interni ───────────────────────────────────────────────────────

    private boolean isActiveTurn() {
        GamePhase phase = gameState.getFase();
        return phase == GamePhase.PLAYER_TURN || phase == GamePhase.ENEMY_TURN;
    }

    private void clearSelection() {
        selectedUnit = null;
        unitHasMoved = false;
        unitHasActed = false;
    }
}
