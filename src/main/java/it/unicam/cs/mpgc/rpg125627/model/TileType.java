package it.unicam.cs.mpgc.rpg125627.model;

/**
 * Categorie di terreno che influenzano il movimento sulla {@link GridMap}.
 */
public enum TileType {

    /** Terreno aperto — sempre percorribile. */
    PLAIN,

    /** Vegetazione fitta — percorribile, ma potrà penalizzare il movimento in regole future. */
    FOREST,

    /** Picchi rocciosi — impercorribili. */
    MOUNTAIN;

    /**
     * Indica se le unità possono entrare in una cella di questo tipo.
     *
     * @return {@code true} per i terreni attraversabili
     */
    public boolean isWalkable() {
        return switch (this) {
            case PLAIN, FOREST -> true;
            case MOUNTAIN      -> false;
        };
    }
}
