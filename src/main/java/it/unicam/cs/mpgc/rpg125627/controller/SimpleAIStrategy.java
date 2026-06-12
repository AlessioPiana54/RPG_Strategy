package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.Ability;
import it.unicam.cs.mpgc.rpg125627.model.ActionState;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Unit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Strategia IA concreta: ogni unità nemica in stato {@link ActionState#READY} o
 * {@link ActionState#MOVED} si avvicina all'unità giocatore più vicina (BFS entro la gittata
 * di movimento) e attacca con la prima abilità offensiva disponibile se il bersaglio è in
 * range dopo il movimento. Se nessun bersaglio è raggiungibile l'unità viene comunque
 * esaurita tramite {@link Unit#onActed()}.
 */
public class SimpleAIStrategy implements AIStrategy {

    @Override
    public void playTurn(GameState state, GameController controller) {
        List<Unit> enemies = new ArrayList<>(state.getUnitaVive(Team.ENEMY));

        for (Unit enemy : enemies) {
            if (state.isOver()) break;
            if (!enemy.isAlive()) continue;

            ActionState as = enemy.getActionState();
            if (as == ActionState.EXHAUSTED) continue;

            Optional<Unit> closest = findClosestPlayer(enemy, state);
            if (closest.isEmpty()) {
                // Nessun bersaglio disponibile: esaurisci l'unità senza agire
                if (as == ActionState.READY) enemy.onMoved();
                enemy.onActed();
                continue;
            }

            Unit target = closest.get();

            // Fase movimento: solo se READY
            if (as == ActionState.READY) {
                controller.selectUnit(enemy.getPosizione());
                Position bestMove = findBestMovePosition(enemy, target.getPosizione(), state.getMappa());
                if (!bestMove.equals(enemy.getPosizione())) {
                    controller.moveSelectedUnit(bestMove);
                } else {
                    // Nessun movimento utile: marca come MOVED manualmente per permettere l'azione
                    enemy.onMoved();
                }
            } else {
                // Già MOVED: seleziona per poter usare l'abilità
                controller.selectUnit(enemy.getPosizione());
            }

            // Fase azione: usa abilità se in range, altrimenti esaurisci comunque
            Optional<Ability> ability = findOffensiveAbility(enemy, target, state.getMappa());
            if (ability.isPresent()) {
                controller.useAbility(ability.get(), target.getPosizione());
            } else {
                enemy.onActed();
            }
        }
    }

    // ── Metodi ausiliari ─────────────────────────────────────────────────────

    private Optional<Unit> findClosestPlayer(Unit enemy, GameState state) {
        return state.getUnitaVive(Team.PLAYER).stream()
            .min(Comparator.comparingInt(p -> enemy.getPosizione().distanceTo(p.getPosizione())));
    }

    /**
     * BFS dalla posizione corrente dell'unità: raccoglie tutte le celle raggiungibili entro
     * {@code moveRange} passi, poi restituisce quella con la distanza di Manhattan minore
     * rispetto a {@code targetPos}. Restituisce la posizione corrente dell'unità se non esiste
     * una cella migliore.
     */
    private Position findBestMovePosition(Unit unit, Position targetPos, GridMap map) {
        int moveRange = unit.getClasseUnita().getMoveRange();
        Position start = unit.getPosizione();

        Set<Position> reachable = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();
        Map<Position, Integer> steps = new HashMap<>();

        reachable.add(start);
        queue.add(start);
        steps.put(start, 0);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            int dist = steps.get(current);
            if (dist >= moveRange) continue;

            for (Position neighbour : map.getAdjacentPositions(current)) {
                if (!reachable.contains(neighbour) && map.isWalkable(neighbour)) {
                    reachable.add(neighbour);
                    queue.add(neighbour);
                    steps.put(neighbour, dist + 1);
                }
            }
        }

        return reachable.stream()
            .min(Comparator.comparingInt(p -> p.distanceTo(targetPos)))
            .orElse(start);
    }

    /** Restituisce la prima abilità che può raggiungere {@code target} dalla posizione corrente. */
    private Optional<Ability> findOffensiveAbility(Unit unit, Unit target, GridMap map) {
        return unit.getAbilita().stream()
            .filter(ability -> ability.isValidTarget(unit, target))
            .findFirst();
    }
}
