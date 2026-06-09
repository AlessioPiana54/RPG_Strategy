package it.unicam.cs.mpgc.rpg125627.model;

/**
 * Contratto comune per qualsiasi entità che partecipa al combattimento.
 * Le implementazioni possono essere unità del giocatore, nemici o futuri tipi di boss.
 */
public interface Combatant {

    /**
     * @return il nome visualizzato di questo combattente
     */
    String getName();

    /**
     * @return punti vita correnti (sempre ≥ 0)
     */
    int getHp();

    /**
     * @return punti vita massimi di questo combattente
     */
    int getMaxHp();

    /**
     * @return {@code true} se il combattente ha almeno 1 PV
     */
    boolean isAlive();

    /**
     * @return la {@link Team} a cui appartiene questo combattente
     */
    Team getTeam();
}
