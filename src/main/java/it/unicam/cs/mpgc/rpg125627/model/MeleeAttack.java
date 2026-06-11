package it.unicam.cs.mpgc.rpg125627.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Colpo fisico a corto raggio che infligge una quantità fissa di danno.
 * Pensato per unità con {@code attackRange == 1}.
 */
@XmlRootElement(name = "melee")
@XmlAccessorType(XmlAccessType.FIELD)
public final class MeleeAttack implements Ability {

    @XmlAttribute private String name;
    @XmlAttribute private int danno;

    /** Costruttore senza argomenti richiesto da JAXB. */
    protected MeleeAttack() {}

    /**
     * @param name  nome visualizzato (es. "Colpo di Spada")
     * @param danno danno grezzo inflitto prima di eventuali futuri modificatori di difesa
     */
    public MeleeAttack(String name, int danno) {
        if (danno < 0) throw new IllegalArgumentException("Il danno non può essere negativo");
        this.name  = name;
        this.danno = danno;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getDescription() {
        return "Infligge " + danno + " danni a un bersaglio adiacente.";
    }

    @Override
    public void apply(Unit sorgente, Unit bersaglio) {
        bersaglio.takeDamage(danno);
    }

    /** @return valore grezzo di danno di questo attacco */
    public int getDanno() { return danno; }
}
