package com.laserarena.Controller;

import com.laserarena.Model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.laserarena.Repository.*;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BilletListController {

    @FXML private TableView<Billet>                 billetTable;
    @FXML private TableColumn<Billet, String>       colCode;
    @FXML private TableColumn<Billet, StatutBillet> colStatut;
    @FXML private TableColumn<Billet, LocalDate>    colExpiration;
    @FXML private TableColumn<Billet, Void>         colQr;
    @FXML private Label                             sessionNomLabel;
    @FXML private Label                             billetCountLabel;

    private Session session;
    private final BilletRepository       billetRepository  = new BilletRepository();
    private final QRCodeBilletRepository qrCodeRepository  = new QRCodeBilletRepository();
    private final SessionRepository      sessionRepository = new SessionRepository();

    public void setSession(Session session) {
        this.session = session;
        sessionNomLabel.setText(session.getNom());
        chargerBillets();
    }

    public void init() {
        sessionNomLabel.setText("Tous les billets");
        chargerBillets();
    }

    @FXML
    public void initialize() {
        colCode      .setCellValueFactory(new PropertyValueFactory<>("code"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(StatutBillet statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); return; }
                Label badge = new Label(statut.name());
                String style = switch (statut) {
                    case VALIDE  -> "-fx-background-color:#E1F5EE;-fx-text-fill:#085041;";
                    case UTILISE -> "-fx-background-color:#E6F1FB;-fx-text-fill:#185FA5;";
                    default      -> "-fx-background-color:#FCEBEB;-fx-text-fill:#A32D2D;";
                };
                badge.setStyle(style + "-fx-padding:2 8;-fx-background-radius:20;");
                setGraphic(badge);
                setText(null);
            }
        });

        colQr.setCellFactory(col -> new TableCell<>() {
            private final Button btnQr    = new Button("Afficher QR");
            private final Button btnSuppr = new Button("Supprimer");
            private final HBox   box      = new HBox(6, btnQr, btnSuppr);
            {
                btnQr   .setStyle("-fx-font-size:12px;-fx-padding:4 10;-fx-border-color:#e0e0e0;-fx-border-width:0.5;-fx-background-color:transparent;-fx-cursor:hand;");
                btnSuppr.setStyle("-fx-font-size:12px;-fx-padding:4 10;-fx-border-color:#f09595;-fx-border-width:0.5;-fx-background-color:transparent;-fx-text-fill:#a32d2d;-fx-cursor:hand;");

                btnQr.setOnAction(e -> {
                    Billet b = getTableView().getItems().get(getIndex());
                    afficherQRCode(b);
                });

                btnSuppr.setOnAction(e -> {
                    Billet b = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer le billet \"" + b.getCode() + "\" ?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES) supprimerBillet(b);
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    public void onGenererBillet() {
        if (session != null) {
            // On est dans la page d'une session précise : générer directement, sans dialog
            genererBilletPourSession(session);
        } else {
            // On est dans "tous les billets" : demander quelle session
            Task<List<Session>> loadTask = new Task<>() {
                @Override protected List<Session> call() { return sessionRepository.findAll(); }
            };
            loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
                List<Session> sessions = loadTask.getValue();
                if (sessions.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Aucune session disponible. Créez d'abord une session.").showAndWait();
                    return;
                }
                afficherDialogChoisirSession(sessions);
            }));
            new Thread(loadTask).start();
        }
    }

    private void afficherDialogChoisirSession(List<Session> sessions) {
        Dialog<Session> dialog = new Dialog<>();
        dialog.setTitle("Générer un billet");
        dialog.setHeaderText("Choisissez la session pour ce billet");

        ButtonType genererBtn = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(genererBtn, ButtonType.CANCEL);

        ComboBox<Session> sessionCombo = new ComboBox<>();
        sessionCombo.getItems().setAll(sessions);
        sessionCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Session s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNom() + " (" + s.getMaxJoueurs() + " joueurs max)");
            }
        });
        sessionCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Session s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNom() + " (" + s.getMaxJoueurs() + " joueurs max)");
            }
        });
        sessionCombo.setMaxWidth(Double.MAX_VALUE);
        sessionCombo.getSelectionModel().selectFirst();

        Label infoLabel = new Label();
        infoLabel.setStyle("-fx-font-size: 12px;");

        Runnable updateInfo = () -> {
            Session sel = sessionCombo.getValue();
            if (sel == null) return;
            int count    = sessionRepository.countBillets(sel.getId());
            int restants = sel.getMaxJoueurs() - count;
            if (restants <= 0) {
                infoLabel.setText("⚠ Capacité maximale atteinte (" + count + "/" + sel.getMaxJoueurs() + ")");
                infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #c0392b;");
                dialog.getDialogPane().lookupButton(genererBtn).setDisable(true);
            } else {
                infoLabel.setText("✓ " + count + "/" + sel.getMaxJoueurs() + " billets — " + restants + " place(s) disponible(s)");
                infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #085041;");
                dialog.getDialogPane().lookupButton(genererBtn).setDisable(false);
            }
        };

        sessionCombo.valueProperty().addListener((obs, old, nw) -> updateInfo.run());
        updateInfo.run();

        VBox content = new VBox(10, new Label("Session :"), sessionCombo, infoLabel);
        content.setStyle("-fx-padding: 10 0;");
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(bt -> bt == genererBtn ? sessionCombo.getValue() : null);
        dialog.showAndWait().ifPresent(this::genererBilletPourSession);
    }

    private void genererBilletPourSession(Session sessionCible) {
        int count = sessionRepository.countBillets(sessionCible.getId());
        if (count >= sessionCible.getMaxJoueurs()) {
            new Alert(Alert.AlertType.WARNING,
                    "Impossible : la session \"" + sessionCible.getNom() + "\" a atteint sa capacité maximale de "
                            + sessionCible.getMaxJoueurs() + " billet(s).").showAndWait();
            return;
        }

        Billet billet = new Billet(
                UUID.randomUUID().toString(),
                StatutBillet.VALIDE,
                sessionCible.getDateExpiration(),
                sessionCible
        );

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                billetRepository.save(billet);
                qrCodeRepository.genererEtSauvegarder(billet);
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(this::chargerBillets));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).showAndWait();
        }));
        new Thread(task).start();
    }

    private void supprimerBillet(Billet billet) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { billetRepository.delete(billet.getId()); return null; }
        };
        task.setOnSucceeded(e -> Platform.runLater(this::chargerBillets));
        task.setOnFailed(e -> Platform.runLater(() ->
                new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression.").showAndWait()));
        new Thread(task).start();
    }

    private void afficherQRCode(Billet billet) {
        Task<byte[]> task = new Task<>() {
            @Override protected byte[] call() { return qrCodeRepository.findImageByBillet(billet); }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            byte[] imageBytes = task.getValue();
            if (imageBytes == null) return;
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(280);
            imageView.setFitHeight(280);

            Label codeLabel = new Label(billet.getCode());
            codeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-padding: 6 0 0 0;");
            codeLabel.setWrapText(true);

            VBox root = new VBox(8, imageView, codeLabel);
            root.setStyle("-fx-padding: 16; -fx-alignment: center; -fx-background-color: white;");

            Stage stage = new Stage();
            stage.setTitle("QR Code — " + billet.getCode().substring(0, 8) + "...");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            // Pas de initModality → fenêtre non-modale, l'appli reste cliquable
            stage.show();
        }));
        new Thread(task).start();
    }
    private void chargerBillets() {
        Task<ObservableList<Billet>> task = new Task<>() {
            @Override protected ObservableList<Billet> call() {
                if (session != null)
                    return FXCollections.observableArrayList(billetRepository.findBySession(session));
                else
                    return FXCollections.observableArrayList(billetRepository.findAll());
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            billetTable.setItems(task.getValue());
            billetCountLabel.setText(task.getValue().size() + " billet(s)");
        }));
        new Thread(task).start();
    }

    @FXML public void onNavSessions() { NavigationHelper.navigateTo("SessionListView.fxml"); }
    @FXML public void onNavBillets()  { NavigationHelper.navigateToBillets(); }
    @FXML public void onNavScanner()  { NavigationHelper.navigateTo("ScannerView.fxml"); }
}