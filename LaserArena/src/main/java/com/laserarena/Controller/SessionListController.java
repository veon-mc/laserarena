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
import javafx.scene.layout.HBox;

public class SessionListController {

    @FXML private TableView<Session>                  sessionTable;
    @FXML private TableColumn<Session, String>        colNom;
    @FXML private TableColumn<Session, String>        colType;
    @FXML private TableColumn<Session, Integer>       colDuree;
    @FXML private TableColumn<Session, Integer>       colMaxJoueurs;
    @FXML private TableColumn<Session, StatutSession> colStatut;
    @FXML private TableColumn<Session, Void>          colActions;
    @FXML private Label                               sessionCountLabel;

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
            protected void updateItem(StatutSession statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); return; }
                Label badge = new Label(statut.name());
                if (statut == StatutSession.OUVERTE) {
                    badge.setStyle("-fx-background-color:#E1F5EE; -fx-text-fill:#085041; -fx-padding:2 8; -fx-background-radius:20;");
                } else {
                    badge.setStyle("-fx-background-color:#F1EFE8; -fx-text-fill:#5F5E5A; -fx-padding:2 8; -fx-background-radius:20;");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnBillets = new Button("Voir billets");
            private final Button btnSuppr   = new Button("Supprimer");
            private final HBox   box        = new HBox(6, btnBillets, btnSuppr);
            {
                btnBillets.setStyle("-fx-font-size:12px; -fx-padding:4 10; -fx-border-color:#e0e0e0; -fx-border-width:0.5; -fx-background-color:transparent; -fx-cursor:hand;");
                btnSuppr  .setStyle("-fx-font-size:12px; -fx-padding:4 10; -fx-border-color:#f09595; -fx-border-width:0.5; -fx-background-color:transparent; -fx-text-fill:#a32d2d; -fx-cursor:hand;");

                btnBillets.setOnAction(e -> {
                    Session s = getTableView().getItems().get(getIndex());
                    NavigationHelper.navigateToWithSession("BilletListView.fxml", s);
                });

                btnSuppr.setOnAction(e -> {
                    Session s = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer la session \"" + s.getNom() + "\" et tous ses billets ?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES) supprimerSession(s);
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        chargerSessions();
    }

    private void supprimerSession(Session session) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                sessionRepository.delete(session.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(this::chargerSessions));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression.");
            alert.showAndWait();
        }));
        new Thread(task).start();
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
    @FXML public void onNavSessions()     { /* déjà sur cette vue */ }
    @FXML public void onNavBillets()      { NavigationHelper.navigateToBillets(); }
    @FXML public void onNavScanner()      { NavigationHelper.navigateTo("ScannerView.fxml"); }
}