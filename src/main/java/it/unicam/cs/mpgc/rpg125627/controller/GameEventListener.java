package it.unicam.cs.mpgc.rpg125627.controller;

/**
 * Interfaccia combinata per i listener che devono reagire sia agli eventi di unità
 * sia ai cambi di turno. Estende {@link UnitEventListener} e {@link TurnEventListener},
 * ereditandone le implementazioni di default vuote.
 *
 * <p>I listener che necessitano solo di un sottoinsieme di eventi possono implementare
 * direttamente {@link UnitEventListener} o {@link TurnEventListener} ed essere registrati
 * tramite i metodi dedicati in {@link BattleEngine}.</p>
 */
public interface GameEventListener extends UnitEventListener, TurnEventListener {
}
