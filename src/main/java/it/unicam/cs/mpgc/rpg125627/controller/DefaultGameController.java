package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Ability;
import it.unicam.cs.mpgc.rpg125627.model.ActionState;
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

    /**
     * Seleziona l'unità alla posizione indicata.
     * Rifiuta se l'unità non appartiene al team corrente, è morta o è {@link ActionState#EXHAUSTED}.
     */
    @Override
    public void selectUnit(Position position) {
        Objects.requireNonNull(position, "position");
        if (gameState.isOver()) return;

        Team expectedTeam = gameState.getFase() == GamePhase.PLAYER_TURN
            ? Team.PLAYER : Team.ENEMY;

        gameState.getMappa().getUnit(position).ifPresent(unit -> {
            if (unit.getTeam() == expectedTeam
                    && unit.isAlive()
                    && unit.getActionState() != ActionState.EXHAUSTED) {
                selectedUnit = unit;
            }
        });
    }

    /**
     * Sposta l'unità selezionata verso {@code target}.
     * Rifiuta se l'unità non è in stato {@link ActionState#READY}.
     */
    @Override
    public void moveSelectedUnit(Position target) {
        Objects.requireNonNull(target, "target");
        if (selectedUnit == null || !isActiveTurn()) return;
        if (selectedUnit.getActionState() != ActionState.READY) return;

        MoveCommand cmd = new MoveCommand(selectedUnit, target, battleEngine, gameState.getMappa());
        cmd.execute();
        commandHistory.push(cmd);
        selectedUnit.onMoved();
    }

    /**
     * Usa {@code ability} puntando a {@code targetPosition}.
     * Rifiuta se l'unità selezionata è {@link ActionState#EXHAUSTED}.
     */
    @Override
    public void useAbility(Ability ability, Position targetPosition) {
        Objects.requireNonNull(ability,         "ability");
        Objects.requireNonNull(targetPosition,  "targetPosition");
        if (selectedUnit == null || !isActiveTurn()) return;
        if (selectedUnit.getActionState() == ActionState.EXHAUSTED) return;

        gameState.getMappa().getUnit(targetPosition).ifPresent(target -> {
            AbilityCommand cmd = new AbilityCommand(
                selectedUnit, ability, target, battleEngine, gameState.getMappa());
            cmd.execute();
            commandHistory.push(cmd);
            selectedUnit.onActed();
            gameState.checkVictory();
        });
    }

    /**
     * Termina il turno del giocatore, esegue il turno nemico e ripristina il turno del giocatore.
     *
     * <p>Sequenza:</p>
     * <ol>
     *   <li>Imposta ENEMY_TURN e notifica i listener</li>
     *   <li>Esegue {@link SimpleAIStrategy#playTurn}</li>
     *   <li>Chiama {@code resetForNewTurn()} su tutte le unità</li>
     *   <li>Imposta PLAYER_TURN (incrementando il contatore turni in {@link GameState})</li>
     *   <li>Notifica {@code onTurnChanged} a tutti i listener registrati</li>
     * </ol>
     */
    @Override
    public void endTurn() {
        if (gameState.isOver()) return;

        clearSelection();
        commandHistory.clear();

        // 1. PLAYER_TURN → ENEMY_TURN + notifica
        gameState.nextTurn();
        battleEngine.fireTurnChanged(Team.ENEMY, gameState.getTurnoCorrente());
        turnManager.resetTurn();

        // 2. Esegui turno IA
        if (!gameState.isOver()) {
            aiStrategy.playTurn(gameState, this);
        }

        // 3. resetForNewTurn su tutte le unità
        gameState.getUnita().forEach(Unit::resetForNewTurn);

        // 4-5. ENEMY_TURN → PLAYER_TURN (incrementa turnoCorrente) + notifica
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
     * Annulla l'ultimo comando di movimento eseguito durante il turno corrente del giocatore.
     * Solo i {@link MoveCommand} supportano completamente l'annullamento;
     * gli {@link AbilityCommand} no.
     *
     * @return {@code true} se un comando è stato annullato
     */
    public boolean undoLastCommand() {
        if (commandHistory.isEmpty()) return false;
        GameCommand cmd = commandHistory.peek();
        if (!(cmd instanceof MoveCommand)) return false;
        commandHistory.pop();
        cmd.undo();
        if (selectedUnit != null) {
            selectedUnit.resetForNewTurn();
        }
        return true;
    }

    /** Restituisce l'unità attualmente selezionata, o {@code null} se nessuna è selezionata. */
    public Unit getSelectedUnit() {
        return selectedUnit;
    }

    /**
     * Restituisce {@code true} se tutte le unità PLAYER sono {@link ActionState#EXHAUSTED},
     * indicando che il turno può terminare automaticamente.
     */
    public boolean isEndTurnAvailable() {
        return gameState.getUnitaVive(Team.PLAYER).stream()
            .allMatch(u -> u.getActionState() == ActionState.EXHAUSTED);
    }

    // ── Metodi interni ───────────────────────────────────────────────────────

    private boolean isActiveTurn() {
        GamePhase phase = gameState.getFase();
        return phase == GamePhase.PLAYER_TURN || phase == GamePhase.ENEMY_TURN;
    }

    private void clearSelection() {
        selectedUnit = null;
    }
}
