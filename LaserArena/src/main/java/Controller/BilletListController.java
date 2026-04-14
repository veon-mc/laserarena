package Controller;

import Model.Billet;
import Model.Session;
import Model.StatutBillet;
import Repository.BilletRepository;
import Repository.QRCodeBilletRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.UUID;

public class BilletListController {

    @FXML private TableView<Billet>              billetTable;
    @FXML private TableColumn<Billet, String>    colCode;
    @FXML private TableColumn<Billet, String>    colStatut;
    @FXML private TableColumn<Billet, LocalDate> colExpiration;
    @FXML private TableColumn<Billet, Void>      colQr;
    @FXML private Label                          sessionNomLabel;
    @FXML private Label                          billetCountLabel;

    private Session           session;
    private final BilletRepository      billetRepository      = new BilletRepository();
    private final QRCodeBilletRepository qrCodeRepository     = new QRCodeBilletRepository();

    // Appelé par NavigationHelper pour injecter la session courante
    public void setSession(Session session) {
        this.session = session;
        sessionNomLabel.setText(session.getNom());
        chargerBillets();
    }

    @FXML
    public void initialize() {
        colCode      .setCellValueFactory(new PropertyValueFactory<>("code"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); return; }
                Label badge = new Label(statut);
                String style = switch (statut) {
                    case "VALIDE"  -> "-fx-background-color:#E1F5EE;-fx-text-fill:#085041;";
                    case "UTILISE" -> "-fx-background-color:#E6F1FB;-fx-text-fill:#185FA5;";
                    default        -> "-fx-background-color:#FCEBEB;-fx-text-fill:#A32D2D;";
                };
                badge.setStyle(style + "-fx-padding:2 8;-fx-background-radius:20;");
                setGraphic(badge);
                setText(null);
            }
        });

        colQr.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Afficher QR");
            {
                btn.setStyle("-fx-font-size:12px;-fx-padding:4 10;-fx-border-color:#e0e0e0;-fx-border-width:0.5;-fx-background-color:transparent;-fx-cursor:hand;");
                btn.setOnAction(e -> {
                    Billet b = getTableView().getItems().get(getIndex());
                    afficherQRCode(b);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // ------------------------------------------------------------------ //
    //  Générer un nouveau billet pour la session                          //
    // ------------------------------------------------------------------ //
    @FXML
    public void onGenererBillet() {
        Billet billet = new Billet(
                UUID.randomUUID().toString(),
                StatutBillet.VALIDE,
                session.getDateExpiration(),
                session
        );

        // Sauvegarde BDD + génération QR en arrière-plan
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                billetRepository.save(billet);
                qrCodeRepository.genererEtSauvegarder(billet);
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(this::chargerBillets));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du billet.");
            alert.showAndWait();
        }));

        new Thread(task).start();
    }

    // ------------------------------------------------------------------ //
    //  Afficher la popup avec le QR code du billet                       //
    // ------------------------------------------------------------------ //
    private void afficherQRCode(Billet billet) {
        Task<byte[]> task = new Task<>() {
            @Override
            protected byte[] call() {
                return qrCodeRepository.findImageByBillet(billet);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            byte[] imageBytes = task.getValue();
            if (imageBytes == null) return;
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(250);
            imageView.setFitHeight(250);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("QR Code — " + billet.getCode());
            alert.setHeaderText(null);
            alert.getDialogPane().setContent(imageView);
            alert.showAndWait();
        }));

        new Thread(task).start();
    }

    // ------------------------------------------------------------------ //
    //  Chargement des billets en arrière-plan                             //
    // ------------------------------------------------------------------ //
    private void chargerBillets() {
        Task<ObservableList<Billet>> task = new Task<>() {
            @Override
            protected ObservableList<Billet> call() {
                return FXCollections.observableArrayList(
                        billetRepository.findBySession(session)
                );
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            billetTable.setItems(task.getValue());
            billetCountLabel.setText(task.getValue().size() + " billet(s)");
        }));

        new Thread(task).start();
    }

    @FXML public void onNavSessions() { NavigationHelper.navigateTo("SessionListView.fxml"); }
    @FXML public void onNavBillets()  { /* déjà sur cette vue */ }
    @FXML public void onNavScanner()  { NavigationHelper.navigateTo("ScannerView.fxml"); }
}