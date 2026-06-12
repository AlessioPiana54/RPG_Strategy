package it.unicam.cs.mpgc.rpg125627.view;

import it.unicam.cs.mpgc.rpg125627.model.ActionState;
import it.unicam.cs.mpgc.rpg125627.model.GridMap;
import it.unicam.cs.mpgc.rpg125627.model.Position;
import it.unicam.cs.mpgc.rpg125627.model.Team;
import it.unicam.cs.mpgc.rpg125627.model.Tile;
import it.unicam.cs.mpgc.rpg125627.model.TileType;
import it.unicam.cs.mpgc.rpg125627.model.Unit;
import it.unicam.cs.mpgc.rpg125627.model.UnitClass;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Griglia visuale della mappa di gioco. Ogni cella è uno StackPane con:
 * <ol>
 *   <li>Rettangolo di sfondo colorato in base al tipo di terreno</li>
 *   <li>Rettangolo overlay semi-trasparente (celle raggiungibili / bersagliabili)</li>
 *   <li>Forma simbolo unità (cerchio/stella/diamante) con mini-barra HP</li>
 * </ol>
 * La cella selezionata pulsa tramite {@link ScaleTransition}.
 * I danni e le cure mostrano numeri fluttuanti con {@link TranslateTransition}.
 */
public class MapView extends GridPane {

    private static final int    CELL_SIZE = 60;
    private static final double GAP       = 2;
    private static final int    HP_BAR_W  = 44;

    private final GridMap map;
    private final Map<Position, StackPane> cells = new HashMap<>();
    private Consumer<Position> onCellClicked;

    /** Animazione pulsante sulla cella selezionata (null se nessuna). */
    private ScaleTransition pulseAnimation;
    private StackPane       pulsedCell;

    public MapView(GridMap map) {
        this.map = map;
        setHgap(GAP);
        setVgap(GAP);
        setPadding(new Insets(6));
        getStyleClass().add("map-grid");
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
        stack.getStyleClass().add("map-cell");

        // Layer 0 – terreno
        Tile tile = map.getTile(pos);
        Rectangle bg = new Rectangle(CELL_SIZE, CELL_SIZE);
        bg.setFill(terrainColor(tile.getTipo()));
        stack.getChildren().add(bg);

        // Layer 1 – overlay semi-trasparente
        Rectangle overlay = new Rectangle(CELL_SIZE, CELL_SIZE);
        overlay.setFill(Color.TRANSPARENT);
        overlay.setId("overlay");
        overlay.setMouseTransparent(true);
        stack.getChildren().add(overlay);

        // Simbolo terreno Unicode (angolo in alto a sinistra)
        String sym = terrainSymbol(tile.getTipo());
        if (!sym.isEmpty()) {
            Label symLabel = new Label(sym);
            symLabel.getStyleClass().add("terrain-label");
            symLabel.setMouseTransparent(true);
            symLabel.setPadding(new Insets(2, 0, 0, 3));
            StackPane.setAlignment(symLabel, Pos.TOP_LEFT);
            stack.getChildren().add(symLabel);
        }

        stack.setOnMouseClicked(e -> { if (onCellClicked != null) onCellClicked.accept(pos); });
        return stack;
    }

    // ── Aggiornamento ─────────────────────────────────────────────────────────

    /** Ridisegna le unità su tutte le celle (terreno invariato). */
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

        // Rimuovi forma, label e barra HP precedenti; i float damage restano (si auto-rimuovono)
        stack.getChildren().removeIf(n -> {
            String id = n.getId();
            return "unitShape".equals(id) || "unitLabel".equals(id) || "hpBar".equals(id);
        });

        map.getUnit(pos).ifPresent(unit -> {
            // ── Forma classe-specifica ──────────────────────────────────────────
            Shape shape = buildUnitShape(unit);

            // ── Label simbolo Unicode ────────────────────────────────────────────
            Label lbl = new Label(unitSymbol(unit.getClasseUnita()));
            lbl.setId("unitLabel");
            lbl.getStyleClass().add("unit-map-label");
            lbl.setMouseTransparent(true);

            Tooltip.install(shape, new Tooltip(
                unit.getName() + " (" + unit.getClasseUnita() + ")"
                + "  HP " + unit.getHp() + "/" + unit.getMaxHp()
                + "  [" + unit.getActionState() + "]"
            ));

            if (unit.getActionState() == ActionState.EXHAUSTED) {
                shape.setOpacity(0.4);
                lbl.setOpacity(0.4);
            }

            // Abbassa forma e simbolo per lasciare spazio alla barra HP in alto
            shape.setTranslateY(5);
            lbl.setTranslateY(5);

            stack.getChildren().addAll(shape, lbl);

            // ── Mini-barra HP ────────────────────────────────────────────────────
            stack.getChildren().add(buildHpBar(unit));

            // ── Bordo tratteggiato per MOVED ─────────────────────────────────────
            if (unit.getActionState() == ActionState.MOVED) {
                applyMovedBorder(stack);
            }
        });
    }

    /** Crea la forma grafica in base alla classe dell'unità. */
    private Shape buildUnitShape(Unit unit) {
        boolean player = unit.getTeam() == Team.PLAYER;
        Color fill   = player ? Color.rgb(50, 140, 230) : Color.rgb(220, 55, 55);
        Color stroke = player ? Color.rgb(160, 210, 255) : Color.rgb(255, 160, 160);

        Shape shape = switch (unit.getClasseUnita()) {
            // Cerchio — solido e difensivo come un guerriero
            case WARRIOR -> new Circle(18);
            // Stella a 5 punte — magica e puntuta
            case MAGE    -> buildStar(18, 7, 5);
            // Triangolo — agile come una freccia
            case ARCHER  -> buildRegularPolygon(18, 3);
        };

        shape.setId("unitShape");
        shape.setFill(fill);
        shape.setStroke(stroke);
        shape.setStrokeWidth(2);
        shape.setMouseTransparent(true);

        DropShadow shadow = new DropShadow(7, 1, 2, Color.rgb(0, 0, 0, 0.65));
        shape.setEffect(shadow);
        return shape;
    }

    /** Stella a {@code punti} punte con raggi esterno/interno. */
    private Polygon buildStar(double outer, double inner, int punti) {
        Polygon star = new Polygon();
        for (int i = 0; i < punti * 2; i++) {
            double angle = Math.PI * i / punti - Math.PI / 2;
            double r = (i % 2 == 0) ? outer : inner;
            star.getPoints().addAll(r * Math.cos(angle), r * Math.sin(angle));
        }
        return star;
    }

    /** Poligono regolare a {@code lati} lati con il primo vertice in cima. */
    private Polygon buildRegularPolygon(double radius, int lati) {
        Polygon p = new Polygon();
        for (int i = 0; i < lati; i++) {
            double angle = 2 * Math.PI * i / lati - Math.PI / 2;
            p.getPoints().addAll(radius * Math.cos(angle), radius * Math.sin(angle));
        }
        return p;
    }

    /** Crea la mini-barra HP (Group con sfondo + riempimento, allineato in alto). */
    private Group buildHpBar(Unit unit) {
        double ratio = unit.getMaxHp() > 0
            ? Math.min(1.0, (double) unit.getHp() / unit.getMaxHp()) : 0;
        int fillW = (int)(HP_BAR_W * ratio);

        Rectangle bg   = new Rectangle(HP_BAR_W, 5, Color.rgb(40, 15, 15));
        Rectangle fill = new Rectangle(Math.max(0, fillW), 5, hpColor(ratio));
        // fill inizia a x=0 del Group → allineato a sinistra del background
        fill.setTranslateX(0);

        Group group = new Group(bg, fill);
        group.setId("hpBar");
        group.setMouseTransparent(true);
        StackPane.setAlignment(group, Pos.TOP_CENTER);
        group.setTranslateY(4);
        return group;
    }

    private Color hpColor(double ratio) {
        if (ratio > 0.6) return Color.rgb(76, 175, 80);
        if (ratio > 0.3) return Color.rgb(255, 152, 0);
        return Color.rgb(244, 67, 54);
    }

    // ── Numeri fluttuanti ─────────────────────────────────────────────────────

    /** Mostra un numero di danno fluttuante sulla cella indicata. */
    public void showDamage(Position pos, int damage) {
        showFloat(pos, "-" + damage, false);
    }

    /** Mostra un numero di cura fluttuante sulla cella indicata. */
    public void showHeal(Position pos, int amount) {
        showFloat(pos, "+" + amount, true);
    }

    private void showFloat(Position pos, String text, boolean isHeal) {
        StackPane cell = cells.get(pos);
        if (cell == null) return;

        Label lbl = new Label(text);
        lbl.getStyleClass().add(isHeal ? "heal-float" : "damage-float");
        lbl.setMouseTransparent(true);
        cell.getChildren().add(lbl);

        TranslateTransition move = new TranslateTransition(Duration.millis(950), lbl);
        move.setFromY(0);
        move.setToY(-58);

        FadeTransition fade = new FadeTransition(Duration.millis(950), lbl);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition anim = new ParallelTransition(move, fade);
        anim.setOnFinished(e -> cell.getChildren().remove(lbl));
        anim.play();
    }

    // ── Evidenziazioni ────────────────────────────────────────────────────────

    /** Rimuove tutti gli overlay e i bordi, ferma l'animazione pulsante. */
    public void clearOverlays() {
        stopPulse();
        for (StackPane stack : cells.values()) {
            stack.getChildren().stream()
                .filter(n -> "overlay".equals(n.getId()))
                .findFirst()
                .ifPresent(n -> ((Rectangle) n).setFill(Color.TRANSPARENT));
            stack.getStyleClass().removeAll("map-cell-selected", "map-cell-moved");
        }
        // Ripristina il bordo tratteggiato delle unità MOVED
        for (int r = 0; r < map.getRighe(); r++) {
            for (int c = 0; c < map.getColonne(); c++) {
                Position p = new Position(r, c);
                map.getUnit(p).ifPresent(unit -> {
                    if (unit.getActionState() == ActionState.MOVED) {
                        applyMovedBorder(cells.get(p));
                    }
                });
            }
        }
    }

    /** Bordo giallo + animazione pulsante sulla cella selezionata. */
    public void highlightSelected(Position pos) {
        stopPulse();
        StackPane cell = cells.get(pos);
        if (cell == null) return;

        cell.getStyleClass().removeAll("map-cell-moved");
        cell.getStyleClass().add("map-cell-selected");
        cell.toFront();

        pulsedCell     = cell;
        pulseAnimation = new ScaleTransition(Duration.millis(600), cell);
        pulseAnimation.setFromX(1.0); pulseAnimation.setFromY(1.0);
        pulseAnimation.setToX(1.04);  pulseAnimation.setToY(1.04);
        pulseAnimation.setAutoReverse(true);
        pulseAnimation.setCycleCount(Animation.INDEFINITE);
        pulseAnimation.play();
    }

    /** Overlay blu per le celle raggiungibili. */
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void applyMovedBorder(StackPane stack) {
        if (stack == null) return;
        stack.getStyleClass().removeAll("map-cell-selected");
        stack.getStyleClass().add("map-cell-moved");
    }

    private void stopPulse() {
        if (pulseAnimation != null) {
            pulseAnimation.stop();
            if (pulsedCell != null) {
                pulsedCell.setScaleX(1.0);
                pulsedCell.setScaleY(1.0);
                pulsedCell.getStyleClass().remove("map-cell-selected");
            }
        }
        pulseAnimation = null;
        pulsedCell     = null;
    }

    private Color terrainColor(TileType type) {
        return switch (type) {
            case PLAIN    -> Color.rgb(115, 185,  75);
            case FOREST   -> Color.rgb(30,  100,  30);
            case MOUNTAIN -> Color.rgb(115, 105,  95);
        };
    }

    private String terrainSymbol(TileType type) {
        return switch (type) {
            case PLAIN    -> "";
            case FOREST   -> "♣";
            case MOUNTAIN -> "▲";
        };
    }

    private String unitSymbol(UnitClass cls) {
        return switch (cls) {
            case WARRIOR -> "⚔";
            case MAGE    -> "★";
            case ARCHER  -> "◎";
        };
    }
}
