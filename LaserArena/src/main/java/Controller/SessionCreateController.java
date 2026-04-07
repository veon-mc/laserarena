package Controller;

import Model.Session;
import Model.StatutSession;
import Model.TypePartie;
import Repository.SessionRepository;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class SessionCreateController {

    @FXML private TextField         nomField;
    @FXML private ComboBox<String>  typePartieCombo;
    @FXML private TextField         dureeField;
    @FXML private TextField         maxJoueursField;
    @FXML private DatePicker        dateExpirationPicker;
    @FXML private Label             errorLabel;

    private final SessionRepository sessionRepository = new SessionRepository();

    @FXML
    public void initialize() {
        typePartieCombo.getItems().setAll(
                TypePartie.ELIMINATION.name(),
                TypePartie.EQUIPES.name(),
                TypePartie.CAPTURE_DRAPEAU.name()
        );
        typePartieCombo.getSelectionModel().selectFirst();
    }

    @FXML
    public void onCreer() {
        if (!validerFormulaire()) return;

        String   nom          = nomField.getText().trim();
        TypePartie type       = TypePartie.valueOf(typePartieCombo.getValue());
        int      duree        = Integer.parseInt(dureeField.getText().trim());
        int      maxJoueurs   = Integer.parseInt(maxJoueursField.getText().trim());
        LocalDate expiration  = dateExpirationPicker.getValue();

        Session session = new Session(nom, type, duree, maxJoueurs, StatutSession.OUVERTE);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                sessionRepository.save(session);
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() ->
                NavigationHelper.navigateTo("SessionListView.fxml")
        ));

        task.setOnFailed(e -> Platform.runLater(() ->
                afficherErreur("Erreur lors de la création de la session.")
        ));

        new Thread(task).start();
    }

    @FXML
    public void onAnnuler() {
        NavigationHelper.navigateTo("SessionListView.fxml");
    }

    // ------------------------------------------------------------------ //
    //  Validation du formulaire                                            //
    // ------------------------------------------------------------------ //
    private boolean validerFormulaire() {
        errorLabel.setText("");

        if (nomField.getText().trim().isEmpty()) {
            afficherErreur("Le nom de la session est obligatoire.");
            return false;
        }
        if (typePartieCombo.getValue() == null) {
            afficherErreur("Veuillez sélectionner un type de partie.");
            return false;
        }
        try {
            int duree = Integer.parseInt(dureeField.getText().trim());
            if (duree <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherErreur("La durée doit être un nombre entier positif.");
            return false;
        }
        try {
            int max = Integer.parseInt(maxJoueursField.getText().trim());
            if (max <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherErreur("Le nombre de joueurs doit être un entier positif.");
            return false;
        }
        if (dateExpirationPicker.getValue() == null) {
            afficherErreur("Veuillez sélectionner une date d'expiration.");
            return false;
        }
        if (dateExpirationPicker.getValue().isBefore(LocalDate.now())) {
            afficherErreur("La date d'expiration ne peut pas être dans le passé.");
            return false;
        }
        return true;
    }

    private void afficherErreur(String message) {
        errorLabel.setText(message);
    }

    @FXML public void onNavSessions() { NavigationHelper.navigateTo("SessionListView.fxml"); }
    @FXML public void onNavBillets()  { NavigationHelper.navigateTo("BilletListView.fxml"); }
    @FXML public void onNavScanner()  { NavigationHelper.navigateTo("ScannerView.fxml"); }
}