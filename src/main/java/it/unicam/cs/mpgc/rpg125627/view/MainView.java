package it.unicam.cs.mpgc.rpg125627.view;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

/**
 * Layout radice dell'applicazione (BorderPane):
 * <ul>
 *   <li>Centro – {@link MapView} in uno ScrollPane</li>
 *   <li>Destra – {@link UnitInfoPanel}</li>
 *   <li>Basso  – {@link ActionBar}</li>
 * </ul>
 */
public class MainView extends BorderPane {

    private final MapView mapView;
    private final UnitInfoPanel unitInfoPanel;
    private final ActionBar actionBar;

    public MainView(MapView mapView, UnitInfoPanel unitInfoPanel, ActionBar actionBar) {
        this.mapView       = mapView;
        this.unitInfoPanel = unitInfoPanel;
        this.actionBar     = actionBar;

        setStyle("-fx-background-color: #1a1a2e;");
        setPadding(new Insets(8));

        ScrollPane mapScroll = new ScrollPane(mapView);
        mapScroll.setFitToHeight(false);
        mapScroll.setFitToWidth(false);
        mapScroll.setPannable(true);
        mapScroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        setCenter(mapScroll);
        setRight(unitInfoPanel);
        setBottom(actionBar);

        BorderPane.setMargin(unitInfoPanel, new Insets(0, 0, 0, 8));
        BorderPane.setMargin(actionBar,     new Insets(8, 0, 0, 0));
    }

    public MapView getMapView()             { return mapView; }
    public UnitInfoPanel getUnitInfoPanel() { return unitInfoPanel; }
    public ActionBar getActionBar()         { return actionBar; }
}
