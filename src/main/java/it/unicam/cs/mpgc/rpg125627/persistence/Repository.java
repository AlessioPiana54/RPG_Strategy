package it.unicam.cs.mpgc.rpg125627.persistence;

import java.util.List;

/**
 * Contratto generico per la persistenza dei dati di gioco.
 *
 * @param <T> il tipo di oggetto da salvare/caricare
 */
public interface Repository<T> {

    /**
     * Serializza {@code data} e lo salva con il nome indicato.
     *
     * @param data il dato da persistere
     * @param name il nome del salvataggio (senza estensione)
     * @throws Exception se la serializzazione o la scrittura su disco fallisce
     */
    void save(T data, String name) throws Exception;

    /**
     * Carica e deserializza il salvataggio con il nome indicato.
     *
     * @param name il nome del salvataggio (senza estensione)
     * @return l'oggetto deserializzato
     * @throws Exception se il file non esiste o la deserializzazione fallisce
     */
    T load(String name) throws Exception;

    /**
     * @return lista dei nomi dei salvataggi disponibili, in ordine alfabetico
     */ // utilizzabile da UI in future implementazioni
    List<String> listSaves();

    /**
     * Elimina il salvataggio con il nome indicato, se esiste.
     *
     * @param name il nome del salvataggio (senza estensione)
     * @throws Exception se la cancellazione fallisce
     */ // utilizzabile da UI in future implementazioni
    void delete(String name) throws Exception;
}
