package it.unicam.cs.mpgc.rpg125627.model;

/**
 * Attacco a proiettile che può raggiungere bersagli oltre le celle adiacenti.
 * È responsabilità del chiamante verificare i vincoli di gittata prima di invocare {@link #apply}.
 */
public final class RangedAttack implements Ability {

    private final String name;
    private final int danno;
    private final int gittata;

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

    /** @return valore grezzo di danno di questo attacco */
    public int getDanno() { return danno; }

    /** @return distanza massima utilizzabile in celle */
    public int getGittata() { return gittata; }
}
