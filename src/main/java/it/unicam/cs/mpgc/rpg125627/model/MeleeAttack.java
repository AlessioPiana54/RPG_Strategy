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
    @XmlAttribute private int contraccolpo;

    /** Costruttore senza argomenti richiesto da JAXB. */
    protected MeleeAttack() {}

    /**
     * @param name  nome visualizzato (es. "Colpo di Spada")
     * @param danno danno grezzo inflitto al bersaglio
     */
    public MeleeAttack(String name, int danno) {
        this(name, danno, 0);
    }

    /**
     * @param name         nome visualizzato (es. "Scudo Frantumato")
     * @param danno        danno grezzo inflitto al bersaglio
     * @param contraccolpo danno inflitto all'attaccante stesso dopo il colpo
     */
    public MeleeAttack(String name, int danno, int contraccolpo) {
        if (danno < 0)        throw new IllegalArgumentException("Il danno non può essere negativo");
        if (contraccolpo < 0) throw new IllegalArgumentException("Il contraccolpo non può essere negativo");
        this.name         = name;
        this.danno        = danno;
        this.contraccolpo = contraccolpo;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getDescription() {
        String base = "Infligge " + danno + " danni a un bersaglio adiacente.";
        return contraccolpo > 0 ? base + " Contraccolpo: " + contraccolpo + " danni all'attaccante." : base;
    }

    @Override
    public void apply(Unit sorgente, Unit bersaglio) {
        bersaglio.takeDamage(danno);
        if (contraccolpo > 0) {
            sorgente.takeDamage(contraccolpo);
        }
    }

    @Override
    public boolean isValidTarget(Unit sorgente, Unit bersaglio) {
        return sorgente.getPosizione().distanceTo(bersaglio.getPosizione()) <= 1;
    }

    /** @return valore grezzo di danno di questo attacco */
    public int getDanno() { return danno; }

    /** @return danno inflitto all'attaccante dopo il colpo (0 = nessun contraccolpo) */
    public int getContraccolpo() { return contraccolpo; }
}
