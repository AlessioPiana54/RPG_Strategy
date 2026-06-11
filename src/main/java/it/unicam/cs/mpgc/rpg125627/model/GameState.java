package it.unicam.cs.mpgc.rpg125627.model;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Stato centrale di una battaglia: contiene la mappa, tutte le unità e tiene traccia
 * del numero di turno corrente e della fase di gioco.
 *
 * <p>Chiama {@link #checkVictory()} dopo ogni azione che potrebbe uccidere un'unità.
 * Una volta che la fase diventa {@link GamePhase#VICTORY} o {@link GamePhase#DEFEAT},
 * la battaglia è conclusa e {@link #nextTurn()} diventa un'operazione nulla.</p>
 */
@XmlRootElement(name = "gameState")
@XmlAccessorType(XmlAccessType.FIELD)
public class GameState {

    @XmlElement(name = "map")
    private GridMap mappa;

    @XmlElement(name = "unit")
    private List<Unit> unita;

    @XmlAttribute private int turnoCorrente;
    @XmlAttribute private GamePhase fase;

    /** Costruttore senza argomenti richiesto da JAXB. */
    protected GameState() {
        this.unita = new ArrayList<>();
    }

    /**
     * Crea un nuovo stato di gioco. La battaglia inizia al turno 1 con il giocatore che agisce per primo.
     *
     * @param mappa la griglia del campo di battaglia
     * @param unita tutte le unità che partecipano alla battaglia (giocatori e nemici)
     */
    public GameState(GridMap mappa, List<Unit> unita) {
        this.mappa         = Objects.requireNonNull(mappa, "mappa");
        this.unita         = new ArrayList<>(Objects.requireNonNull(unita, "unita"));
        this.turnoCorrente = 1;
        this.fase          = GamePhase.PLAYER_TURN;
    }

    // ── Ciclo di vita JAXB ───────────────────────────────────────────────────

    /**
     * Chiamato da JAXB dopo l'unmarshalling: ripristina le unità vive sulla mappa.
     * Necessario perché {@link Tile#occupante} è {@code @XmlTransient}.
     */
    @SuppressWarnings("unused")
    private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (mappa == null || unita == null) return;
        for (Unit unit : unita) {
            if (unit.isAlive() && unit.getPosizione() != null) {
                try {
                    mappa.placeUnit(unit, unit.getPosizione());
                } catch (IllegalStateException ignored) {
                    // Non dovrebbe mai accadere con un salvataggio valido
                }
            }
        }
    }

    // ── Accessori ────────────────────────────────────────────────────────────

    /** @return la griglia del campo di battaglia */
    public GridMap getMappa() { return mappa; }

    /** @return vista in sola lettura di tutte le unità (incluse quelle eliminate) */
    public List<Unit> getUnita() { return Collections.unmodifiableList(unita); }

    /** @return numero di turno corrente (base 1) */
    public int getTurnoCorrente() { return turnoCorrente; }

    /** @return la {@link GamePhase} corrente */
    public GamePhase getFase() { return fase; }

    /** @return {@code true} se la battaglia è terminata (vittoria o sconfitta) */
    public boolean isOver() {
        return fase == GamePhase.VICTORY || fase == GamePhase.DEFEAT;
    }

    // ── Gestione del turno ───────────────────────────────────────────────────

    /**
     * Avanza alla fase di turno successiva.
     * Il ciclo è: {@code PLAYER_TURN → ENEMY_TURN → PLAYER_TURN → …}
     * Il contatore di turno viene incrementato ogni volta che il giocatore inizia un nuovo turno.
     * Non ha effetto se la battaglia è già conclusa.
     */
    public void nextTurn() {
        fase = switch (fase) {
            case PLAYER_TURN -> GamePhase.ENEMY_TURN;
            case ENEMY_TURN  -> {
                turnoCorrente++;
                yield GamePhase.PLAYER_TURN;
            }
            case VICTORY, DEFEAT -> fase;
        };
    }

    /**
     * Valuta le condizioni di vittoria/sconfitta e aggiorna {@link #fase} di conseguenza.
     *
     * <ul>
     *   <li>Tutti i nemici eliminati → {@link GamePhase#VICTORY}</li>
     *   <li>Tutte le unità del giocatore eliminate → {@link GamePhase#DEFEAT}</li>
     *   <li>Nessuna condizione verificata → la fase rimane invariata</li>
     * </ul>
     *
     * <p>Deve essere chiamato dopo ogni azione che potrebbe eliminare un'unità.</p>
     */
    public void checkVictory() {
        if (isOver()) return;

        boolean almenoUnGiocatoreVivo = unita.stream()
                .anyMatch(u -> u.getTeam() == Team.PLAYER && u.isAlive());
        boolean almenoUnNemicoVivo = unita.stream()
                .anyMatch(u -> u.getTeam() == Team.ENEMY && u.isAlive());

        if (!almenoUnNemicoVivo) {
            fase = GamePhase.VICTORY;
        } else if (!almenoUnGiocatoreVivo) {
            fase = GamePhase.DEFEAT;
        }
    }

    /**
     * Aggiunge un'unità alla battaglia (es. per scenari con rinforzi).
     *
     * @param unit l'unità da aggiungere
     */
    public void addUnit(Unit unit) {
        unita.add(Objects.requireNonNull(unit, "unit"));
    }

    /**
     * Restituisce tutte le unità vive appartenenti a {@code team}.
     *
     * @param team la fazione da filtrare
     * @return lista delle unità vive in quella fazione
     */
    public List<Unit> getUnitaVive(Team team) {
        return unita.stream()
                .filter(u -> u.getTeam() == team && u.isAlive())
                .toList();
    }
}
