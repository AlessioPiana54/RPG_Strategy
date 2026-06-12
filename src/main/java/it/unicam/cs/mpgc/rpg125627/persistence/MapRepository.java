package it.unicam.cs.mpgc.rpg125627.persistence;

import it.unicam.cs.mpgc.rpg125627.model.GameState;

import java.util.List;

/**
 * Contratto per il caricamento delle mappe di gioco.
 * Implementazioni concrete possono leggere da XML, JSON, database o altre sorgenti
 * senza richiedere modifiche al codice chiamante.
 */
public interface MapRepository {

    /**
     * Carica la mappa con il nome indicato e restituisce il {@link GameState} iniziale.
     *
     * @param mapName nome della mappa senza estensione
     * @return stato di gioco iniziale
     * @throws MapLoadException se il file non esiste o contiene dati non validi
     */
    GameState loadMap(String mapName) throws MapLoadException;

    /**
     * @return lista ordinata dei nomi delle mappe disponibili (senza estensione)
     */
    List<String> listAvailableMaps();
}
