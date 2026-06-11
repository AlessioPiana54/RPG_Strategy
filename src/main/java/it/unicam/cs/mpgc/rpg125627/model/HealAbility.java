package it.unicam.cs.mpgc.rpg125627.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Ripristina punti vita a un'unità bersaglio senza superarne il massimo.
 * Può essere usata su alleati o sul lanciatore stesso.
 */
@XmlRootElement(name = "heal")
@XmlAccessorType(XmlAccessType.FIELD)
public final class HealAbility implements Ability {

    @XmlAttribute private String name;
    @XmlAttribute private int quantitaCura;

    /** Costruttore senza argomenti richiesto da JAXB. */
    protected HealAbility() {}

    /**
     * @param name         nome visualizzato (es. "Cura")
     * @param quantitaCura PV ripristinati al bersaglio (deve essere positivo)
     */
    public HealAbility(String name, int quantitaCura) {
        if (quantitaCura <= 0) throw new IllegalArgumentException("La quantità di cura deve essere positiva");
        this.name         = name;
        this.quantitaCura = quantitaCura;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getDescription() {
        return "Ripristina " + quantitaCura + " PV al bersaglio.";
    }

    @Override
    public void apply(Unit sorgente, Unit bersaglio) {
        bersaglio.heal(quantitaCura);
    }

    /** @return quantità di PV ripristinati da questa abilità */
    public int getQuantitaCura() { return quantitaCura; }
}
