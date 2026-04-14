package com.laserarena.Controller;

import com.laserarena.Model.Session;
import com.laserarena.Model.StatutSession;
import com.laserarena.Repository.SessionRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;

public class SessionListController {

    @FXML private TableView<Session>            sessionTable;
    @FXML private TableColumn<Session, String>  colNom;
    @FXML private TableColumn<Session, String>  colType;
    @FXML private TableColumn<Session, Integer> colDuree;
    @FXML private TableColumn<Session, Integer> colMaxJoueurs;
    @FXML private TableColumn<Session, String>  colStatut;
    @FXML private TableColumn<Session, Void>    colActions;
    @FXML private Label                         sessionCountLabel;

    private final SessionRepository sessionRepository = new SessionRepository();

    @FXML
    public void initialize() {
        colNom       .setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType      .setCellValueFactory(new PropertyValueFactory<>("typePartie"));
        colDuree     .setCellValueFactory(new PropertyValueFactory<>("dureeMinutes"));
        colMaxJoueurs.setCellValueFactory(new PropertyValueFactory<>("maxJoueurs"));

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); return; }
                Label badge = new Label(statut);
                if (statut.equals(StatutSession.OUVERTE.name())) {
                    badge.setStyle("-fx-background-color:#E1F5EE; -fx-text-fill:#085041; -fx-padding:2 8; -fx-background-radius:20;");
                } else {
                    badge.setStyle("-fx-background-color:#F1EFE8; -fx-text-fill:#5F5E5A; -fx-padding:2 8; -fx-background-radius:20;");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Voir billets");
            {
                btn.setStyle("-fx-font-size:12px; -fx-padding:4 10; -fx-border-color:#e0e0e0; -fx-border-width:0.5; -fx-background-color:transparent; -fx-cursor:hand;");
                btn.setOnAction(e -> {
                    Session s = getTableView().getItems().get(getIndex());
                    NavigationHelper.navigateToWithSession("BilletListView.fxml", s);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        chargerSessions();
    }

    private void chargerSessions() {
        Task<ObservableList<Session>> task = new Task<>() {
            @Override
            protected ObservableList<Session> call() {
                return FXCollections.observableArrayList(sessionRepository.findAll());
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            sessionTable.setItems(task.getValue());
            sessionCountLabel.setText(task.getValue().size() + " session(s)");
        }));

        task.setOnFailed(e -> Platform.runLater(() ->
                sessionCountLabel.setText("Erreur de chargement")
        ));

        new Thread(task).start();
    }

    @FXML public void onNouvelleSession() { NavigationHelper.navigateTo("SessionCreateView.fxml"); }
    @FXML public void onNavSessions()     { /* dÃŠjÃ  sur cette vue */ }
    @FXML public void onNavBillets()      { NavigationHelper.navigateTo("BilletListView.fxml"); }
    @FXML public void onNavScanner()      { NavigationHelper.navigateTo("ScannerView.fxml"); }
}