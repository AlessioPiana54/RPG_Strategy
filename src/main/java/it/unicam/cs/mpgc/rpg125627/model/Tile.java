package it.unicam.cs.mpgc.rpg125627.model;

import java.util.Optional;

/**
 * Singola cella della {@link GridMap}.
 * Ogni cella ha un tipo di terreno fisso e può ospitare al massimo un'{@link Unit}.
 */
public class Tile {

    private final TileType tipo;
    private Unit occupante;

    /**
     * @param tipo categoria di terreno; non deve essere {@code null}
     */
    public Tile(TileType tipo) {
        this.tipo      = tipo;
        this.occupante = null;
    }

    /** @return tipo di terreno di questa cella */
    public TileType getTipo() { return tipo; }

    /**
     * @return {@code true} se un'unità occupa attualmente questa cella
     */
    public boolean isOccupied() { return occupante != null; }

    /**
     * Una cella è percorribile quando il terreno lo consente e nessuna unità la occupa.
     *
     * @return {@code true} se un'unità può entrare in questa cella
     */
    public boolean isWalkable() { return tipo.isWalkable() && !isOccupied(); }

    /**
     * @return l'unità su questa cella, o {@link Optional#empty()} se vuota
     */
    public Optional<Unit> getOccupante() { return Optional.ofNullable(occupante); }

    /**
     * Posiziona un'unità su questa cella.
     *
     * @param unit l'unità da posizionare; {@code null} svuota la cella
     */
    void setOccupante(Unit unit) { this.occupante = unit; }
}
