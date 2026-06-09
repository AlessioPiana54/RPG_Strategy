package it.unicam.cs.mpgc.rpg125627.model;

/**
 * Colpo fisico a corto raggio che infligge una quantità fissa di danno.
 * Pensato per unità con {@code attackRange == 1}.
 */
public final class MeleeAttack implements Ability {

    private final String name;
    private final int danno;

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
