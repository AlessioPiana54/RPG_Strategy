package it.unicam.cs.mpgc.rpg125627.persistence;

import it.unicam.cs.mpgc.rpg125627.controller.GameController;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import jakarta.xml.bind.JAXBException;

import java.util.List;

/**
 * Facade di persistenza esposta dalla view per salvare e caricare la partita corrente.
 * Delega le operazioni di I/O a {@link XmlGameRepository}.
 */
public class SaveManager {

    private final XmlGameRepository repository;
    private GameController controller;

    public SaveManager(XmlGameRepository repository) {
        this.repository = repository;
    }

    /**
     * Imposta il controller da cui leggere il {@link GameState} durante il salvataggio.
     *
     * @param controller il controller attivo
     */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    /**
     * Salva la partita corrente con il nome indicato.
     *
     * @param name nome del file (senza estensione)
     * @throws JAXBException          se la serializzazione XML fallisce
     * @throws IllegalStateException  se il controller non è stato impostato
     */
    public void save(String name) throws JAXBException {
        if (controller == null)
            throw new IllegalStateException("Controller non impostato nel SaveManager");
        repository.save(controller.getGameState(), name);
    }

    /**
     * Carica la partita con il nome indicato e la restituisce al chiamante.
     *
     * @param name nome del file (senza estensione)
     * @return lo {@link GameState} deserializzato
     * @throws JAXBException se il file non esiste o la deserializzazione fallisce
     */
    public GameState load(String name) throws JAXBException {
        return repository.load(name);
    }

    /**
     * @return lista dei salvataggi con nome e data di modifica, dal più recente
     */
    public List<XmlGameRepository.SaveEntry> listSaveEntries() {
        return repository.listSaveEntries();
    }

    /** @return il repository sottostante */
    public XmlGameRepository getRepository() {
        return repository;
    }
}
