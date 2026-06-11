package it.unicam.cs.mpgc.rpg125627.persistence;

/**
 * Eccezione controllata lanciata da {@link MapLoader} quando un file di mappa
 * non esiste, non è leggibile o contiene XML malformato.
 */
public class MapLoadException extends Exception {

    public MapLoadException(String message) {
        super(message);
    }

    public MapLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
