package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gestisce l'ordine di attivazione all'interno di un round: tutte le unità PLAYER agiscono
 * per prime (nell'ordine di inserimento), poi le unità ENEMY. Le unità morte vengono
 * saltate automaticamente.
 */
public class TurnManager {

    private final GameState gameState;
    private List<Unit> roundOrder;
    private int currentIndex;

    public TurnManager(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        buildRoundOrder();
    }

    // ── API pubblica ─────────────────────────────────────────────────────────

    /** Restituisce il team dell'unità attualmente attiva, o PLAYER se il round è terminato. */
    public Team getCurrentTeam() {
        Unit u = getCurrentUnit();
        return u != null ? u.getTeam() : Team.PLAYER;
    }

    /** Restituisce l'unità attualmente attiva, saltando quelle morte a metà round. */
    public Unit getCurrentUnit() {
        skipDeadUnits();
        return currentIndex < roundOrder.size() ? roundOrder.get(currentIndex) : null;
    }

    /**
     * Avanza oltre l'unità corrente e restituisce la successiva,
     * oppure {@code null} se il round è terminato.
     */
    public Unit nextUnit() {
        currentIndex++;
        return getCurrentUnit();
    }

    /** Ricostruisce l'ordine di attivazione dalle unità attualmente in vita (da chiamare a ogni nuovo round). */
    public void resetTurn() {
        buildRoundOrder();
    }

    /** Restituisce {@code true} se almeno un'unità deve ancora agire in questo round. */
    public boolean hasMoreUnits() {
        return getCurrentUnit() != null;
    }

    // ── Metodi interni ───────────────────────────────────────────────────────

    private void buildRoundOrder() {
        roundOrder = new ArrayList<>();
        roundOrder.addAll(gameState.getUnitaVive(Team.PLAYER));
        roundOrder.addAll(gameState.getUnitaVive(Team.ENEMY));
        currentIndex = 0;
        skipDeadUnits();
    }

    private void skipDeadUnits() {
        while (currentIndex < roundOrder.size() && !roundOrder.get(currentIndex).isAlive()) {
            currentIndex++;
        }
    }
}
