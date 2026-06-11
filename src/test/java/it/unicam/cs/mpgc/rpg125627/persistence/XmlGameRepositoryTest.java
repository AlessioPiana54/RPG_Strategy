package it.unicam.cs.mpgc.rpg125627.persistence;

import it.unicam.cs.mpgc.rpg125627.model.GamePhase;
import it.unicam.cs.mpgc.rpg125627.model.GameState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.HealAbility;
import it.unicam.cs.mpgc.rpg125627.model.MeleeAttack;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.RangedAttack;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.TileType;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import it.unicam.cs.mpgc.rpg125627.model.UnitClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica il round-trip save/load di {@link XmlGameRepository}:
 * un {@link GameState} serializzato in XML e ricaricato deve
 * conservare tutti i campi principali.
 */
class XmlGameRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void roundTripConservaFaseETurno() throws Exception {
        GameState originale = buildGameState();
        originale.nextTurn(); // PLAYER_TURN → ENEMY_TURN, turno rimane 1

        XmlGameRepository repo = new XmlGameRepository(tempDir);
        repo.save(originale, "test-turno");
        GameState caricato = repo.load("test-turno");

        assertEquals(originale.getTurnoCorrente(), caricato.getTurnoCorrente());
        assertEquals(GamePhase.ENEMY_TURN, caricato.getFase());
    }

    @Test
    void roundTripConservaUnita() throws Exception {
        GameState originale = buildGameState();
        Unit guerriero = originale.getUnita().get(0);
        guerriero.takeDamage(25); // HP parziali: 120 - 25 = 95

        XmlGameRepository repo = new XmlGameRepository(tempDir);
        repo.save(originale, "test-unita");
        GameState caricato = repo.load("test-unita");

        assertEquals(3, caricato.getUnita().size());

        Unit loadedGuerriero = caricato.getUnita().stream()
                .filter(u -> "Guerriero".equals(u.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Unità 'Guerriero' non trovata dopo il caricamento"));

        assertEquals(guerriero.getHp(),          loadedGuerriero.getHp());
        assertEquals(guerriero.getMaxHp(),        loadedGuerriero.getMaxHp());
        assertEquals(guerriero.getTeam(),          loadedGuerriero.getTeam());
        assertEquals(guerriero.getClasseUnita(),   loadedGuerriero.getClasseUnita());
        assertEquals(guerriero.getPosizione(),     loadedGuerriero.getPosizione());
        assertEquals(guerriero.getAbilita().size(),loadedGuerriero.getAbilita().size());
    }

    @Test
    void roundTripConservaAbilita() throws Exception {
        GameState originale = buildGameState();

        XmlGameRepository repo = new XmlGameRepository(tempDir);
        repo.save(originale, "test-abilita");
        GameState caricato = repo.load("test-abilita");

        Unit mago = caricato.getUnita().stream()
                .filter(u -> "Mago".equals(u.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(2, mago.getAbilita().size());
        assertTrue(mago.getAbilita().get(0) instanceof RangedAttack,
                "Prima abilità del Mago deve essere RangedAttack");
        assertTrue(mago.getAbilita().get(1) instanceof HealAbility,
                "Seconda abilità del Mago deve essere HealAbility");

        RangedAttack sfera = (RangedAttack) mago.getAbilita().get(0);
        assertEquals("Sfera di Fuoco", sfera.getName());
        assertEquals(45, sfera.getDanno());
        assertEquals(4,  sfera.getGittata());
    }

    @Test
    void roundTripConservaMappa() throws Exception {
        GameState originale = buildGameState();

        XmlGameRepository repo = new XmlGameRepository(tempDir);
        repo.save(originale, "test-mappa");
        GameState caricato = repo.load("test-mappa");

        GridMap mappa = caricato.getMappa();
        assertEquals(6, mappa.getRighe());
        assertEquals(8, mappa.getColonne());
        assertEquals(TileType.MOUNTAIN, mappa.getTile(new Position(1, 3)).getTipo());
        assertEquals(TileType.FOREST,   mappa.getTile(new Position(3, 6)).getTipo());
        assertEquals(TileType.PLAIN,    mappa.getTile(new Position(0, 0)).getTipo());
    }

    @Test
    void roundTripRipristinaOccupantiSullaMappa() throws Exception {
        GameState originale = buildGameState();

        XmlGameRepository repo = new XmlGameRepository(tempDir);
        repo.save(originale, "test-occupanti");
        GameState caricato = repo.load("test-occupanti");

        // Guerriero in (0,0), nemico in (5,7)
        assertTrue(caricato.getMappa().getUnit(new Position(0, 0)).isPresent(),
                "Guerriero deve essere ripristinato sulla mappa alla posizione (0,0)");
        assertTrue(caricato.getMappa().getUnit(new Position(5, 7)).isPresent(),
                "Nemico deve essere ripristinato sulla mappa alla posizione (5,7)");
    }

    @Test
    void listSavesRestituisceNomiSalvataggi() throws Exception {
        XmlGameRepository repo = new XmlGameRepository(tempDir);
        repo.save(buildGameState(), "alfa");
        repo.save(buildGameState(), "beta");

        List<String> nomi = repo.listSaves();
        assertTrue(nomi.contains("alfa"));
        assertTrue(nomi.contains("beta"));
        assertEquals(2, nomi.size());
    }

    @Test
    void deleteCancellaIlFile() throws Exception {
        XmlGameRepository repo = new XmlGameRepository(tempDir);
        repo.save(buildGameState(), "da-cancellare");
        assertEquals(1, repo.listSaves().size());

        repo.delete("da-cancellare");
        assertEquals(0, repo.listSaves().size());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Mappa 6×8 con 2 unità (una per team), montagna e foresta. */
    private GameState buildGameState() {
        GridMap mappa = new GridMap(6, 8);
        mappa.setTileType(new Position(1, 3), TileType.MOUNTAIN);
        mappa.setTileType(new Position(3, 6), TileType.FOREST);

        Unit guerriero = new Unit("Guerriero", UnitClass.WARRIOR, Team.PLAYER, new Position(0, 0));
        guerriero.addAbilita(new MeleeAttack("Colpo", 30));

        Unit mago = new Unit("Mago", UnitClass.MAGE, Team.PLAYER, new Position(2, 0));
        mago.addAbilita(new RangedAttack("Sfera di Fuoco", 45, 4));
        mago.addAbilita(new HealAbility("Guarigione", 30));

        Unit nemico = new Unit("Nemico", UnitClass.WARRIOR, Team.ENEMY, new Position(5, 7));
        nemico.addAbilita(new MeleeAttack("Attacco", 25));

        List<Unit> unita = List.of(guerriero, mago, nemico);
        for (Unit u : unita) mappa.placeUnit(u, u.getPosizione());

        return new GameState(mappa, unita);
    }
}
