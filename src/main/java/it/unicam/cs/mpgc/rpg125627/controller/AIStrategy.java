package it.unicam.cs.mpgc.rpg125627.controller;

import it.unicam.cs.mpgc.rpg125627.model.GameState;

/**
 * Pattern Strategy: incapsula il modo in cui la IA nemica decide ed esegue le proprie
 * mosse a ogni turno. Sostituire l'implementazione per variare il comportamento della IA
 * senza modificare il controller.
 */
public interface AIStrategy {
  /**
   * Esegue un turno nemico completo. Le implementazioni devono iterare su tutte le unità
   * nemiche vive, muoversi e attaccare tramite {@code controller}, quindi restituire il controllo.
   * Non devono chiamare {@link GameController#endTurn()}.
   *
   * @param state      lo stato corrente della battaglia (si consiglia una vista in sola lettura)
   * @param controller il controller usato per emettere comandi di movimento e attacco
   */
  void playTurn(GameState state, GameController controller);
}
