package it.unicam.cs.mpgc.rpg125627.model;

import jakarta.xml.bind.annotation.XmlSeeAlso;

/**
 * Capacità sigillata che un'{@link Unit} può utilizzare durante il combattimento.
 * Le implementazioni ammesse coprono i tre archetipi principali:
 * attacco corpo a corpo, attacco a distanza e cura.
 *
 * <p>Essendo sigillata, le espressioni {@code switch} esaustive sulle abilità
 * non richiedono alcun ramo {@code default}, abilitando il pattern matching sicuro.</p>
 */
@XmlSeeAlso({MeleeAttack.class, RangedAttack.class, HealAbility.class})
public sealed interface Ability permits MeleeAttack, RangedAttack, HealAbility {

    /**
     * @return nome breve dell'abilità da visualizzare
     */
    String getName();

    /**
     * @return descrizione leggibile di ciò che l'abilità produce
     */
    String getDescription();

    /**
     * Applica l'effetto dell'abilità da {@code sorgente} a {@code bersaglio}.
     *
     * @param sorgente l'unità che usa l'abilità
     * @param bersaglio l'unità che riceve l'effetto (può coincidere con la sorgente per le autocure)
     */
    void apply(Unit sorgente, Unit bersaglio);

    /**
     * Verifica se {@code bersaglio} è un bersaglio valido per questa abilità usata da {@code sorgente}.
     * Controlla gittata e vincoli di team senza lanciare eccezioni.
     *
     * @param sorgente  l'unità che usa l'abilità
     * @param bersaglio l'unità bersaglio
     * @return {@code true} se il bersaglio è raggiungibile e il vincolo di team è rispettato
     */
    boolean isValidTarget(Unit sorgente, Unit bersaglio);
}
