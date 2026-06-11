package it.unicam.cs.mpgc.rpg125627.model;

import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Griglia rettangolare di celle {@link Tile} che rappresenta il campo di battaglia.
 *
 * <p>La griglia utilizza coordinate (riga, colonna) dove (0,0) è l'angolo in alto a sinistra.
 * Tutti i metodi pubblici validano le posizioni e lanciano {@link IndexOutOfBoundsException}
 * per input fuori dai limiti.</p>
 */
@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public class GridMap {

    @XmlAttribute private int righe;
    @XmlAttribute private int colonne;

    /** Array 2D non serializzato: ricostruito da {@link #tiles} dopo l'unmarshalling. */
    @XmlTransient private Tile[][] griglia;

    /** Lista piatta di dati di terreno usata solo per la serializzazione JAXB. */
    @XmlElement(name = "tile")
    private List<TileData> tiles;

    /** Costruttore senza argomenti richiesto da JAXB. */
    protected GridMap() {}

    /**
     * Crea una mappa interamente composta da celle {@link TileType#PLAIN}.
     *
     * @param righe   numero di righe (deve essere ≥ 1)
     * @param colonne numero di colonne (deve essere ≥ 1)
     */
    public GridMap(int righe, int colonne) {
        if (righe < 1 || colonne < 1)
            throw new IllegalArgumentException("Le dimensioni della griglia devono essere almeno 1x1");
        this.righe   = righe;
        this.colonne = colonne;
        this.griglia = new Tile[righe][colonne];
        for (int r = 0; r < righe; r++)
            for (int c = 0; c < colonne; c++)
                griglia[r][c] = new Tile(TileType.PLAIN);
    }

    /** @return numero di righe della mappa */
    public int getRighe() { return righe; }

    /** @return numero di colonne della mappa */
    public int getColonne() { return colonne; }

    /**
     * Restituisce la cella alla posizione indicata.
     *
     * @param pos posizione target
     * @return la {@link Tile} in quella posizione
     * @throws IndexOutOfBoundsException se la posizione è fuori dai limiti della griglia
     */
    public Tile getTile(Position pos) {
        valida(pos);
        return griglia[pos.row()][pos.col()];
    }

    /**
     * Imposta il tipo di terreno di una cella specifica.
     *
     * @param pos  posizione target
     * @param tipo nuovo tipo di terreno
     */
    public void setTileType(Position pos, TileType tipo) {
        valida(pos);
        griglia[pos.row()][pos.col()] = new Tile(Objects.requireNonNull(tipo, "tipo"));
    }

    /**
     * Restituisce l'unità che occupa {@code pos}, se presente.
     *
     * @param pos la posizione da ispezionare
     * @return un {@link Optional} contenente l'unità, o vuoto se la cella è libera
     */
    public Optional<Unit> getUnit(Position pos) {
        return getTile(pos).getOccupante();
    }

    /**
     * Posiziona un'unità sulla griglia in {@code pos} e aggiorna la posizione interna dell'unità.
     *
     * @param unit l'unità da posizionare
     * @param pos  posizione di destinazione
     * @throws IllegalStateException se la cella di destinazione non è percorribile
     */
    public void placeUnit(Unit unit, Position pos) {
        Objects.requireNonNull(unit, "unit");
        valida(pos);
        Tile cella = griglia[pos.row()][pos.col()];
        if (!cella.isWalkable())
            throw new IllegalStateException("La cella " + pos + " non è percorribile");
        cella.setOccupante(unit);
        unit.move(pos);
    }

    /**
     * Rimuove l'unità da {@code pos}, se presente.
     *
     * @param pos posizione da svuotare
     */
    public void removeUnit(Position pos) {
        valida(pos);
        griglia[pos.row()][pos.col()].setOccupante(null);
    }

    /**
     * Restituisce i quattro vicini cardinali di {@code pos} che rientrano nei limiti della griglia.
     *
     * @param pos posizione centrale
     * @return lista delle posizioni adiacenti valide (fino a 4)
     */
    public List<Position> getAdjacentPositions(Position pos) {
        valida(pos);
        List<Position> vicini = new ArrayList<>(4);
        int r = pos.row(), c = pos.col();
        if (r > 0)        vicini.add(new Position(r - 1, c));
        if (r < righe-1)  vicini.add(new Position(r + 1, c));
        if (c > 0)        vicini.add(new Position(r, c - 1));
        if (c < colonne-1) vicini.add(new Position(r, c + 1));
        return vicini;
    }

    /**
     * Verifica se un'unità può spostarsi in {@code pos}.
     *
     * @param pos la posizione da testare
     * @return {@code true} se la cella esiste, ha terreno percorribile e non è occupata
     */
    public boolean isWalkable(Position pos) {
        if (!isInBounds(pos)) return false;
        return griglia[pos.row()][pos.col()].isWalkable();
    }

    // ── Ciclo di vita JAXB ───────────────────────────────────────────────────

    /** Chiamato da JAXB prima del marshalling: popola {@link #tiles} da {@link #griglia}. */
    @SuppressWarnings("unused")
    private void beforeMarshal(Marshaller marshaller) {
        tiles = new ArrayList<>(righe * colonne);
        for (int r = 0; r < righe; r++)
            for (int c = 0; c < colonne; c++)
                tiles.add(new TileData(r, c, griglia[r][c].getTipo()));
    }

    /** Chiamato da JAXB dopo l'unmarshalling: ricostruisce {@link #griglia} da {@link #tiles}. */
    @SuppressWarnings("unused")
    private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        griglia = new Tile[righe][colonne];
        for (int r = 0; r < righe; r++)
            for (int c = 0; c < colonne; c++)
                griglia[r][c] = new Tile(TileType.PLAIN);
        if (tiles != null) {
            for (TileData td : tiles) {
                griglia[td.row][td.col] = new Tile(td.type);
            }
        }
        tiles = null;
    }

    // ── Metodi di supporto interni ───────────────────────────────────────────

    private boolean isInBounds(Position pos) {
        return pos.row() >= 0 && pos.row() < righe
            && pos.col() >= 0 && pos.col() < colonne;
    }

    private void valida(Position pos) {
        if (!isInBounds(Objects.requireNonNull(pos, "pos")))
            throw new IndexOutOfBoundsException(
                "Posizione " + pos + " fuori dai limiti della griglia " + righe + "x" + colonne);
    }

    // ── Classe interna per la serializzazione JAXB ───────────────────────────

    /** DTO che rappresenta un singolo tile nella lista di serializzazione. */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TileData {
        @XmlAttribute public int row;
        @XmlAttribute public int col;
        @XmlAttribute public TileType type;

        public TileData() {}

        TileData(int row, int col, TileType type) {
            this.row  = row;
            this.col  = col;
            this.type = type;
        }
    }
}
