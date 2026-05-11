package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;
import java.util.UUID;

public class ArtistController {
    @FXML private TextField searchField;
    @FXML private ComboBox<Discipline> disciplineFilter;
    @FXML private TableView<Artist> artistTable;
    @FXML private TableColumn<Artist, String> nameColumn;
    @FXML private TableColumn<Artist, String> cityColumn;
    @FXML private TableColumn<Artist, String> emailColumn;
    @FXML private TableColumn<Artist, Integer> yearColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        showArtistDialog(null).ifPresent(artist -> {
            artist.setArtistId(UUID.randomUUID().toString().substring(0, 8));
            artist.setActive(true);
            try {
                artistService.createArtist(artist);
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de l'ajout", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner un artiste à modifier.");
            return;
        }
        showArtistDialog(selected).ifPresent(updated -> {
            try {
                artistService.updateArtist(updated);
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de la modification", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner un artiste à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'artiste \"" + selected.getName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            try {
                artistService.deleteArtist(selected.getName());
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de la suppression", e.getMessage());
            }
        });
    }

    private Optional<Artist> showArtistDialog(Artist existing) {
        boolean isEdit = (existing != null);
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier un artiste" : "Ajouter un artiste");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField nameField = new TextField(isEdit ? existing.getName() : "");
        nameField.setPromptText("Nom");
        nameField.setDisable(isEdit); // le nom est la clé pour update/delete

        TextField cityField = new TextField(isEdit && existing.getCity() != null ? existing.getCity() : "");
        cityField.setPromptText("Ville");

        TextField emailField = new TextField(isEdit && existing.getContactEmail() != null ? existing.getContactEmail() : "");
        emailField.setPromptText("Email");

        TextField yearField = new TextField(isEdit && existing.getBirthYear() != null ? existing.getBirthYear().toString() : "");
        yearField.setPromptText("Année de naissance");

        TextField bioField = new TextField(isEdit && existing.getBio() != null ? existing.getBio() : "");
        bioField.setPromptText("Biographie");

        grid.add(new Label("Nom :"), 0, 0);        grid.add(nameField, 1, 0);
        grid.add(new Label("Ville :"), 0, 1);       grid.add(cityField, 1, 1);
        grid.add(new Label("Email :"), 0, 2);       grid.add(emailField, 1, 2);
        grid.add(new Label("Année naissance :"), 0, 3); grid.add(yearField, 1, 3);
        grid.add(new Label("Bio :"), 0, 4);         grid.add(bioField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String name = isEdit ? existing.getName() : nameField.getText().trim();
            if (name.isEmpty()) return null;

            Artist a = isEdit ? existing : new Artist();
            a.setName(name);
            a.setCity(cityField.getText().trim());
            a.setContactEmail(emailField.getText().trim());
            a.setBio(bioField.getText().trim());
            try {
                a.setBirthYear(Integer.parseInt(yearField.getText().trim()));
            } catch (NumberFormatException ignored) {
                a.setBirthYear(0);
            }
            return a;
        });

        return dialog.showAndWait();
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
