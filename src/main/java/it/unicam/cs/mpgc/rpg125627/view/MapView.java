package it.unicam.cs.mpgc.rpg125627.view;

import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Tile;
import it.unicam.cs.mpgc.rpg125627.model.TileType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Griglia visuale della mappa di gioco. Ogni cella è uno StackPane con:
 * <ol>
 *   <li>Rettangolo di sfondo colorato in base al tipo di terreno</li>
 *   <li>Rettangolo overlay semi-trasparente (celle raggiungibili / bersagliabili)</li>
 *   <li>Cerchio + label dell'iniziale per le unità presenti</li>
 * </ol>
 * La cella selezionata viene evidenziata con un bordo giallo via CSS.
 */
public class MapView extends GridPane {

    private static final int CELL_SIZE = 60;
    private static final double GAP = 2;

    private final GridMap map;
    private final Map<Position, StackPane> cells = new HashMap<>();
    private Consumer<Position> onCellClicked;

    public MapView(GridMap map) {
        this.map = map;
        setHgap(GAP);
        setVgap(GAP);
        setPadding(new Insets(6));
        setStyle("-fx-background-color: #2a2a2a;");
        buildGrid();
    }

    public void setOnCellClicked(Consumer<Position> handler) {
        this.onCellClicked = handler;
    }

    // ── Costruzione ───────────────────────────────────────────────────────────

    private void buildGrid() {
        getChildren().clear();
        cells.clear();
        for (int r = 0; r < map.getRighe(); r++) {
            for (int c = 0; c < map.getColonne(); c++) {
                Position pos = new Position(r, c);
                StackPane cell = buildCell(pos);
                cells.put(pos, cell);
                add(cell, c, r);
            }
        }
    }

    private StackPane buildCell(Position pos) {
        StackPane stack = new StackPane();
        stack.setPrefSize(CELL_SIZE, CELL_SIZE);
        stack.setMinSize(CELL_SIZE, CELL_SIZE);
        stack.setMaxSize(CELL_SIZE, CELL_SIZE);
        stack.setAlignment(Pos.CENTER);

        // Layer 0 – terreno
        Tile tile = map.getTile(pos);
        Rectangle bg = new Rectangle(CELL_SIZE, CELL_SIZE);
        bg.setFill(terrainColor(tile.getTipo()));
        stack.getChildren().add(bg);

        // Layer 1 – overlay (trasparente per default)
        Rectangle overlay = new Rectangle(CELL_SIZE, CELL_SIZE);
        overlay.setFill(Color.TRANSPARENT);
        overlay.setId("overlay");
        overlay.setMouseTransparent(true);
        stack.getChildren().add(overlay);

        // Simbolo del terreno (angolo in alto a sinistra)
        if (tile.getTipo() != TileType.PLAIN) {
            Label sym = new Label(terrainSymbol(tile.getTipo()));
            sym.setStyle("-fx-font-size: 9; -fx-text-fill: rgba(255,255,255,0.55);");
            sym.setMouseTransparent(true);
            sym.setPadding(new Insets(2, 0, 0, 3));
            StackPane.setAlignment(sym, Pos.TOP_LEFT);
            stack.getChildren().add(sym);
        }

        // Bordo: nessuno per default
        stack.setStyle("-fx-border-color: transparent; -fx-border-width: 3;");

        final Position p = pos;
        stack.setOnMouseClicked(e -> { if (onCellClicked != null) onCellClicked.accept(p); });

        return stack;
    }

    // ── Aggiornamento ─────────────────────────────────────────────────────────

    /** Ridesenha le unità su tutte le celle (terreno invariato). */
    public void refresh() {
        for (int r = 0; r < map.getRighe(); r++) {
            for (int c = 0; c < map.getColonne(); c++) {
                refreshUnit(new Position(r, c));
            }
        }
    }

    private void refreshUnit(Position pos) {
        StackPane stack = cells.get(pos);
        if (stack == null) return;

        stack.getChildren().removeIf(n ->
            "unitCircle".equals(n.getId()) || "unitLabel".equals(n.getId()));

        map.getUnit(pos).ifPresent(unit -> {
            Circle circle = new Circle(21);
            circle.setId("unitCircle");
            circle.setFill(unit.getTeam() == Team.PLAYER
                ? Color.rgb(40, 130, 220)
                : Color.rgb(210, 45, 45));
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(2);
            circle.setMouseTransparent(true);

            Label label = new Label(String.valueOf(unit.getClasseUnita().name().charAt(0)));
            label.setId("unitLabel");
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 15;");
            label.setMouseTransparent(true);

            Tooltip tip = new Tooltip(unit.getName() + " (" + unit.getClasseUnita() + ")"
                + "  HP " + unit.getHp() + "/" + unit.getMaxHp());
            Tooltip.install(circle, tip);

            stack.getChildren().addAll(circle, label);
        });
    }

    // ── Evidenziazioni ────────────────────────────────────────────────────────

    /** Rimuove tutti gli overlay e i bordi. */
    public void clearOverlays() {
        for (StackPane stack : cells.values()) {
            stack.getChildren().stream()
                .filter(n -> "overlay".equals(n.getId()))
                .findFirst()
                .ifPresent(n -> ((Rectangle) n).setFill(Color.TRANSPARENT));
            stack.setStyle("-fx-border-color: transparent; -fx-border-width: 3;");
        }
    }

    /** Bordo giallo sulla cella selezionata. */
    public void highlightSelected(Position pos) {
        StackPane cell = cells.get(pos);
        if (cell != null) {
            cell.setStyle("-fx-border-color: #FFD700; -fx-border-width: 3;");
        }
    }

    /** Overlay blu per le celle raggiungibili dall'unità selezionata. */
    public void showReachable(Set<Position> positions) {
        setOverlay(positions, Color.color(0.1, 0.45, 1.0, 0.35));
    }

    /** Overlay rosso per i bersagli di attacco. */
    public void showTargetable(Set<Position> positions) {
        setOverlay(positions, Color.color(1.0, 0.2, 0.1, 0.40));
    }

    /** Overlay verde per i bersagli di cura. */
    public void showHealTargetable(Set<Position> positions) {
        setOverlay(positions, Color.color(0.1, 0.8, 0.25, 0.40));
    }

    private void setOverlay(Set<Position> positions, Color color) {
        for (Position pos : positions) {
            StackPane cell = cells.get(pos);
            if (cell == null) continue;
            cell.getChildren().stream()
                .filter(n -> "overlay".equals(n.getId()))
                .findFirst()
                .ifPresent(n -> ((Rectangle) n).setFill(color));
        }
    }

    // ── Colori ───────────────────────────────────────────────────────────────

    private Color terrainColor(TileType type) {
        return switch (type) {
            case PLAIN    -> Color.rgb(115, 185, 75);
            case FOREST   -> Color.rgb(30,  100, 30);
            case MOUNTAIN -> Color.rgb(115, 105, 95);
        };
    }

    private String terrainSymbol(TileType type) {
        return switch (type) {
            case PLAIN    -> "";
            case FOREST   -> "F";
            case MOUNTAIN -> "M";
        };
    }
}
