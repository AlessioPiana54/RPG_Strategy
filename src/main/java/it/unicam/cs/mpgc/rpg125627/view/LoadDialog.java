package it.unicam.cs.mpgc.rpg125627.view;

import it.unicam.cs.mpgc.rpg125627.persistence.XmlGameRepository.SaveEntry;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;

/**
 * Finestra di dialogo che mostra la lista dei salvataggi disponibili con
 * nome e data di ultima modifica. Restituisce il nome del salvataggio
 * selezionato, o {@code null} se l'utente annulla.
 */
public class LoadDialog extends Dialog<String> {

    public LoadDialog(Window owner, List<SaveEntry> entries) {
        initOwner(owner);
        setTitle("Carica partita");
        setHeaderText("Seleziona un salvataggio:");

        // ── Lista dei salvataggi ──────────────────────────────────────────────
        ListView<SaveEntry> listView = new ListView<>();
        listView.getItems().addAll(entries);
        listView.setPrefSize(380, 220);
        listView.setStyle(
                "-fx-background-color: #111; -fx-control-inner-background: #1a1a2e;" +
                "-fx-text-fill: #ccc; -fx-font-size: 12;");

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SaveEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name() + "   [" + item.modifiedDate() + "]");
                    setStyle("-fx-text-fill: #eee; -fx-font-size: 12;");
                }
            }
        });

        Label hint = new Label("Doppio clic o OK per caricare");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10;");

        VBox content = new VBox(6, listView, hint);
        getDialogPane().setContent(content);
        getDialogPane().setStyle("-fx-background-color: #1a1a2e;");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // OK disabilitato finché non c'è una selezione
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> okButton.setDisable(sel == null));

        // Doppio clic equivale a OK
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                okButton.fire();
            }
        });

        setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                SaveEntry selected = listView.getSelectionModel().getSelectedItem();
                return selected != null ? selected.name() : null;
            }
            return null;
        });
    }
}
