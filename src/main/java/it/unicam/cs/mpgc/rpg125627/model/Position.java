package it.unicam.cs.mpgc.rpg125627.model;

/**
 * Coordinata immutabile sulla griglia espressa come coppia (riga, colonna).
 * Utilizza la distanza di Manhattan come metrica standard per il movimento su griglia.
 *
 * @param row indice di riga a base zero (dall'alto verso il basso)
 * @param col indice di colonna a base zero (da sinistra verso destra)
 */
public record Position(int row, int col) {

    /**
     * Calcola la distanza di Manhattan tra questa posizione e {@code altra}.
     *
     * @param altra la posizione di destinazione
     * @return distanza in celle (valore non negativo)
     */
    public int distanceTo(Position altra) {
        return Math.abs(this.row - altra.row) + Math.abs(this.col - altra.col);
    }
}
