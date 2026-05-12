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

/**
 * Controller responsible for displaying and managing the list of lasergame sessions.
 * <p>
 * Displays all sessions in a {@link TableView} with their name, game type, duration,
 * maximum player count and status. Provides the following features:
 * <ul>
 *     <li>Viewing the tickets associated with a specific session.</li>
 *     <li>Deleting a session (along with all its tickets) after confirmation.</li>
 *     <li>Navigating to the session creation view.</li>
 * </ul>
 * All database operations are performed asynchronously.
 * </p>
 */
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

    /**
     * FXML initialization method called automatically after the view is loaded.
     * <p>
     * Configures the {@link TableView} columns:
     * <ul>
     *     <li>{@code colNom}: displays the session name.</li>
     *     <li>{@code colType}: displays the game type.</li>
     *     <li>{@code colDuree}: displays the duration in minutes.</li>
     *     <li>{@code colMaxJoueurs}: displays the maximum number of players.</li>
     *     <li>{@code colStatut}: displays the status as a colored badge
     *         (green for OUVERTE, grey for TERMINEE).</li>
     *     <li>{@code colActions}: displays "View tickets" and "Delete" action buttons.</li>
     * </ul>
     * Triggers an initial load of all sessions from the database.
     * </p>
     */
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
                        if (bt == ButtonType.YES) deleteSession(s);
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        loadSessions();
    }

    /**
     * Deletes a session and all its associated tickets from the database asynchronously.
     * Reloads the session list on success. Displays an error alert if the operation fails.
     *
     * @param session the session to delete
     */
    private void deleteSession(Session session) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                sessionRepository.delete(session.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(this::loadSessions));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression.");
            alert.showAndWait();
        }));
        new Thread(task).start();
    }

    /**
     * Loads all sessions from the database asynchronously and updates
     * the {@link TableView} along with the session count label.
     * Displays an error message in the count label if loading fails.
     */
    private void loadSessions() {
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

    /**
     * Navigates to the session creation view.
     */
    @FXML public void onNewSession() { NavigationHelper.navigateTo("SessionCreateView.fxml"); }

    /**
     * Navigation action for the session list view (no-op since this is the current view).
     */
    @FXML public void onNavSessions()     { /* already on this view */ }

    /**
     * Navigates to the ticket list view showing all tickets.
     */
    @FXML public void onNavBillets()      { NavigationHelper.navigateToBillets(); }

    /**
     * Navigates to the ticket scanner view.
     */
    @FXML public void onNavScanner()      { NavigationHelper.navigateTo("ScannerView.fxml"); }
}