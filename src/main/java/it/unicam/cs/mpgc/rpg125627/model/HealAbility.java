package it.unicam.cs.mpgc.rpg125627.model;

/**
 * Ripristina punti vita a un'unità bersaglio senza superarne il massimo.
 * Può essere usata su alleati o sul lanciatore stesso.
 */
public final class HealAbility implements Ability {

    private final String name;
    private final int quantitaCura;

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
