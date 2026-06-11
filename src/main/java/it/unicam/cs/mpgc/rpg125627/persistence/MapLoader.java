package it.unicam.cs.mpgc.rpg125627.persistence;

import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.ScenarioFactory;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Carica le mappe XML da {@code src/main/resources/maps/} e le converte
 * in un {@link GameState} iniziale tramite {@link ScenarioFactory}.
 *
 * <p>Usa il {@link JAXBContext} condiviso del progetto ({@link XmlGameRepository#getContext()})
 * anziché crearne uno nuovo. La deserializzazione avviene con il metodo tipizzato
 * {@code unmarshal(Source, Class)} per gestire correttamente la condivisione del
 * nome radice {@code "map"} con {@link it.unicam.cs.mpgc.rpg125627.model.GridMap}.</p>
 */
public class MapLoader {

    /**
     * Carica la mappa con il nome indicato da {@code maps/<nome>.xml} nel classpath
     * e restituisce il {@link GameState} iniziale costruito da {@link ScenarioFactory}.
     *
     * @param mapName nome della mappa senza estensione (es. {@code "map_forest"})
     * @return stato di gioco iniziale
     * @throws MapLoadException se il file non esiste o contiene XML malformato
     */
    public GameState loadMap(String mapName) throws MapLoadException {
        String resourcePath = "maps/" + mapName + ".xml";
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new MapLoadException("File mappa non trovato nel classpath: " + resourcePath);
        }
        try {
            Unmarshaller u = XmlGameRepository.getContext().createUnmarshaller();
            XmlMap xmlMap = u.unmarshal(new StreamSource(is), XmlMap.class).getValue();
            return ScenarioFactory.build(xmlMap);
        } catch (JAXBException e) {
            throw new MapLoadException(
                    "Errore di parsing della mappa '" + mapName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Elenca i nomi delle mappe disponibili (senza estensione) presenti in
     * {@code src/main/resources/maps/}.
     *
     * <p>Il listing funziona quando l'applicazione è avviata dal filesystem (sviluppo/Gradle);
     * all'interno di un JAR fat restituisce una lista vuota.</p>
     *
     * @return lista ordinata dei nomi delle mappe
     */
    public List<String> listAvailableMaps() {
        URL url = getClass().getClassLoader().getResource("maps");
        if (url == null) return List.of();
        try {
            File dir = new File(url.toURI());
            if (!dir.isDirectory()) return List.of();
            File[] files = dir.listFiles(f -> f.getName().endsWith(".xml"));
            if (files == null) return List.of();
            return Arrays.stream(files)
                    .map(f -> f.getName().replace(".xml", ""))
                    .sorted()
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
