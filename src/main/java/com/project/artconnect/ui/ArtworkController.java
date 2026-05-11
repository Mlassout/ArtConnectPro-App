package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;
import java.util.UUID;

public class ArtworkController {
    @FXML private TableView<Artwork> artworkTable;
    @FXML private TableColumn<Artwork, String> titleColumn;
    @FXML private TableColumn<Artwork, String> typeColumn;
    @FXML private TableColumn<Artwork, Double> priceColumn;
    @FXML private TableColumn<Artwork, String> statusColumn;
    @FXML private TableColumn<Artwork, String> artistColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));

        refreshTable();
    }

    @FXML
    private void handleAdd() {
        showArtworkDialog(null).ifPresent(artwork -> {
            artwork.setArtworkId(UUID.randomUUID().toString().substring(0, 8));
            try {
                artworkService.createArtwork(artwork);
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de l'ajout", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner une œuvre à modifier.");
            return;
        }
        showArtworkDialog(selected).ifPresent(updated -> {
            try {
                artworkService.updateArtwork(updated);
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de la modification", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner une œuvre à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'œuvre \"" + selected.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            try {
                artworkService.deleteArtwork(selected.getTitle());
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de la suppression", e.getMessage());
            }
        });
    }

    private Optional<Artwork> showArtworkDialog(Artwork existing) {
        boolean isEdit = (existing != null);
        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier une œuvre" : "Ajouter une œuvre");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField titleField = new TextField(isEdit ? existing.getTitle() : "");
        titleField.setPromptText("Titre");
        titleField.setDisable(isEdit); // le titre est la clé pour update/delete

        TextField typeField = new TextField(isEdit && existing.getType() != null ? existing.getType() : "");
        typeField.setPromptText("Type (peinture, sculpture...)");

        TextField priceField = new TextField(isEdit ? String.valueOf(existing.getPrice()) : "");
        priceField.setPromptText("Prix");

        TextField yearField = new TextField(isEdit && existing.getCreationYear() != null ? existing.getCreationYear().toString() : "");
        yearField.setPromptText("Année de création");

        ComboBox<Artwork.Status> statusBox = new ComboBox<>();
        statusBox.setItems(FXCollections.observableArrayList(Artwork.Status.values()));
        statusBox.setValue(isEdit && existing.getStatus() != null ? existing.getStatus() : Artwork.Status.FOR_SALE);

        ComboBox<Artist> artistBox = new ComboBox<>();
        artistBox.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        if (isEdit && existing.getArtist() != null) {
            artistService.getArtistByName(existing.getArtist().getName()).ifPresent(artistBox::setValue);
        }
        artistBox.setDisable(isEdit); // l'artiste ne peut pas être changé sans modifier Artist_Id en DB

        grid.add(new Label("Titre :"), 0, 0);          grid.add(titleField, 1, 0);
        grid.add(new Label("Type :"), 0, 1);           grid.add(typeField, 1, 1);
        grid.add(new Label("Prix :"), 0, 2);           grid.add(priceField, 1, 2);
        grid.add(new Label("Année création :"), 0, 3); grid.add(yearField, 1, 3);
        grid.add(new Label("Statut :"), 0, 4);         grid.add(statusBox, 1, 4);
        grid.add(new Label("Artiste :"), 0, 5);        grid.add(artistBox, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String title = isEdit ? existing.getTitle() : titleField.getText().trim();
            if (title.isEmpty()) return null;
            Artist artist = isEdit ? existing.getArtist() : artistBox.getValue();
            if (!isEdit && artist == null) return null;

            Artwork a = isEdit ? existing : new Artwork();
            a.setTitle(title);
            a.setType(typeField.getText().trim());
            a.setStatus(statusBox.getValue());
            a.setArtist(artist);
            try {
                a.setPrice(Double.parseDouble(priceField.getText().trim()));
            } catch (NumberFormatException ignored) {
                a.setPrice(0.0);
            }
            try {
                a.setCreationYear(Integer.parseInt(yearField.getText().trim()));
            } catch (NumberFormatException ignored) {
                a.setCreationYear(0);
            }
            return a;
        });

        return dialog.showAndWait();
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
