package it.unicam.cs.mpgc.rpg125627.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Attacco a proiettile che può raggiungere bersagli oltre le celle adiacenti.
 * È responsabilità del chiamante verificare i vincoli di gittata prima di invocare {@link #apply}.
 */
@XmlRootElement(name = "ranged")
@XmlAccessorType(XmlAccessType.FIELD)
public final class RangedAttack implements Ability {

    @XmlAttribute private String name;
    @XmlAttribute private int danno;
    @XmlAttribute private int gittata;

    /** Costruttore senza argomenti richiesto da JAXB. */
    protected RangedAttack() {}

    /**
     * @param name    nome visualizzato (es. "Tiro con l'Arco")
     * @param danno   danno grezzo inflitto al bersaglio
     * @param gittata distanza massima in celle a cui questo attacco può essere usato
     */
    public RangedAttack(String name, int danno, int gittata) {
        if (danno < 0)   throw new IllegalArgumentException("Il danno non può essere negativo");
        if (gittata < 1) throw new IllegalArgumentException("La gittata deve essere almeno 1");
        this.name    = name;
        this.danno   = danno;
        this.gittata = gittata;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getDescription() {
        return "Infligge " + danno + " danni a un bersaglio entro " + gittata + " celle.";
    }

    @Override
    public void apply(Unit sorgente, Unit bersaglio) {
        bersaglio.takeDamage(danno);
    }

    @Override
    public boolean isValidTarget(Unit sorgente, Unit bersaglio) {
        return sorgente.getPosizione().distanceTo(bersaglio.getPosizione()) <= gittata;
    }

    /** @return valore grezzo di danno di questo attacco */
    public int getDanno() { return danno; }

    /** @return distanza massima utilizzabile in celle */
    public int getGittata() { return gittata; }
}
