package it.unicam.cs.mpgc.rpg125627.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Combattente concreto sul campo di battaglia, dotato di posizione, archetipo e insieme di abilità.
 *
 * <p>I PV sono sempre limitati all'intervallo {@code [0, maxHp]}. Un'unità è considerata
 * viva finché {@code hp > 0}. La validazione di gittata di movimento e di turno spetta
 * al livello di logica di gioco, non a questa classe.</p>
 */
@XmlRootElement(name = "unit")
@XmlAccessorType(XmlAccessType.FIELD)
public class Unit implements Combatant {

    @XmlAttribute private String name;
    @XmlAttribute private UnitClass classeUnita;
    @XmlAttribute private Team team;
    @XmlAttribute private int maxHp;
    @XmlAttribute private int hp;

    @XmlElement(name = "position")
    private Position posizione;

    @XmlElements({
        @XmlElement(name = "melee",  type = MeleeAttack.class),
        @XmlElement(name = "ranged", type = RangedAttack.class),
        @XmlElement(name = "heal",   type = HealAbility.class)
    })
    private List<Ability> abilita = new ArrayList<>();

    /** Costruttore senza argomenti richiesto da JAXB. */
    protected Unit() {}

    /**
     * Crea un'unità posizionata in {@code posizioneIniziale}.
     *
     * @param name               nome visualizzato
     * @param classeUnita        archetipo che fornisce le statistiche di base
     * @param team               fazione per cui combatte l'unità
     * @param posizioneIniziale  cella di partenza sulla griglia
     */
    public Unit(String name, UnitClass classeUnita, Team team, Position posizioneIniziale) {
        this.name         = Objects.requireNonNull(name, "name");
        this.classeUnita  = Objects.requireNonNull(classeUnita, "classeUnita");
        this.team         = Objects.requireNonNull(team, "team");
        this.posizione    = Objects.requireNonNull(posizioneIniziale, "posizioneIniziale");
        this.maxHp        = classeUnita.getBaseHp();
        this.hp           = this.maxHp;
        this.abilita      = new ArrayList<>();
    }

    // ── Combatant ────────────────────────────────────────────────────────────

    @Override public String  getName()  { return name; }
    @Override public int     getHp()    { return hp; }
    @Override public int     getMaxHp() { return maxHp; }
    @Override public boolean isAlive()  { return hp > 0; }
    @Override public Team    getTeam()  { return team; }

    // ── Accessori ────────────────────────────────────────────────────────────

    /** @return l'archetipo di questa unità */
    public UnitClass getClasseUnita() { return classeUnita; }

    /** @return posizione corrente sulla griglia */
    public Position getPosizione() { return posizione; }

    /** @return vista in sola lettura delle abilità possedute dall'unità */
    public List<Ability> getAbilita() { return Collections.unmodifiableList(abilita); }

    // ── Mutazioni ────────────────────────────────────────────────────────────

    /**
     * Aggiunge un'abilità al repertorio di questa unità.
     *
     * @param abilita l'abilità da aggiungere
     */
    public void addAbilita(Ability abilita) {
        this.abilita.add(Objects.requireNonNull(abilita, "abilita"));
    }

    /**
     * Aggiorna la posizione dell'unità sulla griglia.
     * La validazione della gittata (confronto con {@link UnitClass#getMoveRange()}) è responsabilità del chiamante.
     *
     * @param nuovaPosizione cella di destinazione
     */
    public void move(Position nuovaPosizione) {
        this.posizione = Objects.requireNonNull(nuovaPosizione, "nuovaPosizione");
    }

    /**
     * Riduce i PV di {@code danno}, con minimo a zero.
     *
     * @param danno quantità di danno non negativa
     * @throws IllegalArgumentException se {@code danno} è negativo
     */
    public void takeDamage(int danno) {
        if (danno < 0) throw new IllegalArgumentException("Il danno non può essere negativo");
        hp = Math.max(0, hp - danno);
    }

    /**
     * Aumenta i PV di {@code quantita}, con massimo a {@code maxHp}.
     *
     * @param quantita PV da ripristinare (deve essere positiva)
     * @throws IllegalArgumentException se {@code quantita} non è positiva
     */
    public void heal(int quantita) {
        if (quantita <= 0) throw new IllegalArgumentException("La quantità di cura deve essere positiva");
        hp = Math.min(maxHp, hp + quantita);
    }

    /**
     * Applica {@code abilita} da questa unità a {@code bersaglio}.
     *
     * @param abilita  l'abilità da usare — deve appartenere al repertorio di questa unità
     * @param bersaglio il destinatario dell'effetto
     * @throws IllegalArgumentException se l'unità non possiede l'abilità indicata
     */
    public void useAbility(Ability abilita, Unit bersaglio) {
        if (!this.abilita.contains(Objects.requireNonNull(abilita, "abilita"))) {
            throw new IllegalArgumentException(name + " non possiede l'abilità: " + abilita.getName());
        }
        abilita.apply(this, Objects.requireNonNull(bersaglio, "bersaglio"));
    }

    @Override
    public String toString() {
        return name + " [" + classeUnita + "/" + team + " PV:" + hp + "/" + maxHp + " @" + posizione + "]";
    }
}
