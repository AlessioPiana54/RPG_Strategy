package it.unicam.cs.mpgc.rpg125627.model;

/**
 * Stato d'azione di un'unità all'interno del turno corrente.
 *
 * <ul>
 *   <li>{@link #READY}     — può muoversi e agire</li>
 *   <li>{@link #MOVED}     — si è già mossa; può ancora agire ma non muoversi di nuovo</li>
 *   <li>{@link #EXHAUSTED} — ha completato il turno; non può fare nulla fino al prossimo round</li>
 * </ul>
 */
public enum ActionState {
    READY,
    MOVED,
    EXHAUSTED
}
