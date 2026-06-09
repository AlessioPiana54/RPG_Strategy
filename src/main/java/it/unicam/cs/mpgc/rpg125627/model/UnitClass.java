package it.unicam.cs.mpgc.rpg125627.model;

/**
 * Definisce l'archetipo di un'unità insieme alle sue statistiche di base.
 * Ogni costante incapsula HP massimi, gittata di movimento (in celle) e gittata di attacco (in celle).
 */
public enum UnitClass {

    /** Combattente in prima linea, corazzato e con portata ridotta. */
    WARRIOR(120, 3, 1),

    /** Incantatore fragile ma con gittata di attacco elevata. */
    MAGE(70, 2, 4),

    /** Combattente a distanza equilibrato tra mobilità e portata. */
    ARCHER(90, 3, 3);

    private final int baseHp;
    private final int moveRange;
    private final int attackRange;

    UnitClass(int baseHp, int moveRange, int attackRange) {
        this.baseHp      = baseHp;
        this.moveRange   = moveRange;
        this.attackRange = attackRange;
    }

    /** @return punti vita massimi per le unità di questa classe */
    public int getBaseHp() { return baseHp; }

    /** @return numero massimo di celle percorribili per turno */
    public int getMoveRange() { return moveRange; }

    /** @return distanza massima in celle per l'uso degli attacchi */
    public int getAttackRange() { return attackRange; }
}
