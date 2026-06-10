package it.unicam.cs.mpgc.rpg125627.controller;

/**
 * Pattern Command: incapsula una singola azione di gioco in modo che possa essere
 * eseguita, accodata e, facoltativamente, annullata.
 */
public interface GameCommand {

    void execute();

    /**
     * Annulla l'effetto di {@link #execute()}.
     * Le implementazioni che non supportano l'annullamento lanciano {@link UnsupportedOperationException}.
     */
    default void undo() {
        throw new UnsupportedOperationException("Questo comando non supporta l'annullamento");
    }
}
