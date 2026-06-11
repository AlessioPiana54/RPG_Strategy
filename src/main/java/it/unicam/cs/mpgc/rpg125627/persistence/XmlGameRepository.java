package it.unicam.cs.mpgc.rpg125627.persistence;

import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.HealAbility;
import it.unicam.cs.mpgc.rpg125627.model.MeleeAttack;
import it.unicam.cs.mpgc.rpg125627.model.RangedAttack;
import it.unicam.cs.mpgc.rpg125627.model.Tile;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Implementazione di {@link Repository} che serializza/deserializza {@link GameState}
 * in XML tramite JAXB, salvando i file in {@code ~/.tacticarpg/saves/}.
 */
public class XmlGameRepository implements Repository<GameState> {

    private static final Path DEFAULT_SAVES_DIR =
            Path.of(System.getProperty("user.home"), ".tacticarpg", "saves");

    private static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(
                    GameState.class,
                    GridMap.class,
                    GridMap.TileData.class,
                    Unit.class,
                    MeleeAttack.class,
                    RangedAttack.class,
                    HealAbility.class,
                    Tile.class
            );
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError(
                    "Inizializzazione JAXBContext fallita: " + e.getMessage());
        }
    }

    private final Path savesDir;

    /** Usa la directory di salvataggio predefinita ({@code ~/.tacticarpg/saves/}). */
    public XmlGameRepository() {
        this(DEFAULT_SAVES_DIR);
    }

    /**
     * Usa una directory personalizzata — utile nei test con directory temporanee.
     *
     * @param savesDir directory in cui verranno salvati i file XML
     */
    public XmlGameRepository(Path savesDir) {
        this.savesDir = savesDir;
    }

    // ── Repository<GameState> ─────────────────────────────────────────────────

    @Override
    public void save(GameState data, String name) throws JAXBException {
        try {
            Files.createDirectories(savesDir);
        } catch (IOException e) {
            throw new JAXBException(
                    "Impossibile creare la directory dei salvataggi '" + savesDir + "': " + e.getMessage(), e);
        }
        Path file = savesDir.resolve(name + ".xml");
        createMarshaller().marshal(data, file.toFile());
    }

    @Override
    public GameState load(String name) throws JAXBException {
        Path file = savesDir.resolve(name + ".xml");
        if (!Files.exists(file)) {
            throw new JAXBException("File di salvataggio non trovato: " + file);
        }
        Unmarshaller u = CONTEXT.createUnmarshaller();
        return (GameState) u.unmarshal(file.toFile());
    }

    @Override
    public List<String> listSaves() {
        if (!Files.exists(savesDir)) return List.of();
        try (Stream<Path> stream = Files.list(savesDir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .map(p -> p.getFileName().toString().replace(".xml", ""))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public void delete(String name) throws IOException {
        Files.deleteIfExists(savesDir.resolve(name + ".xml"));
    }

    // ── API aggiuntiva ────────────────────────────────────────────────────────

    /**
     * Restituisce l'elenco dei salvataggi con nome e data di ultima modifica,
     * ordinati dal più recente al più vecchio.
     *
     * @return lista di {@link SaveEntry}
     */
    public List<SaveEntry> listSaveEntries() {
        if (!Files.exists(savesDir)) return List.of();
        try (Stream<Path> stream = Files.list(savesDir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .sorted(Comparator.comparing(p -> {
                        try { return Files.getLastModifiedTime(p); }
                        catch (IOException e) { return FileTime.fromMillis(0); }
                    }, Comparator.reverseOrder()))
                    .map(this::toSaveEntry)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    // ── Metodi interni ────────────────────────────────────────────────────────

    private Marshaller createMarshaller() throws JAXBException {
        Marshaller m = CONTEXT.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return m;
    }

    private SaveEntry toSaveEntry(Path path) {
        String name = path.getFileName().toString().replace(".xml", "");
        String date;
        try {
            date = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(Files.getLastModifiedTime(path).toInstant());
        } catch (IOException e) {
            date = "Data sconosciuta";
        }
        return new SaveEntry(name, date);
    }

    // ── Tipi di supporto ─────────────────────────────────────────────────────

    /**
     * Informazioni su un singolo file di salvataggio.
     *
     * @param name         nome del salvataggio (senza estensione)
     * @param modifiedDate data/ora di ultima modifica formattata
     */
    public record SaveEntry(String name, String modifiedDate) {}
}
