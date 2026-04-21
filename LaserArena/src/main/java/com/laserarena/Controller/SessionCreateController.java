package com.laserarena.Controller;

import com.laserarena.Model.Billet;
import com.laserarena.Model.Session;
import com.laserarena.Model.StatutBillet;
import com.laserarena.Model.StatutSession;
import com.laserarena.Model.TypePartie;
import com.laserarena.Repository.BilletRepository;
import com.laserarena.Repository.QRCodeBilletRepository;
import com.laserarena.Repository.SessionRepository;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SessionCreateController {

    @FXML private TextField         nomField;
    @FXML private ComboBox<String>  typePartieCombo;
    @FXML private DatePicker        dateExpirationPicker;
    @FXML private Label             errorLabel;

    // Durée - boutons toggle
    @FXML private ToggleButton btn15;
    @FXML private ToggleButton btn30;
    @FXML private ToggleButton btn45;
    @FXML private ToggleButton btn60;
    private final ToggleGroup  dureeGroup = new ToggleGroup();
    private int dureeSelectionnee = 0;

    // Panel joueurs simples (ELIMINATION)
    @FXML private VBox      panelJoueursSimple;
    @FXML private TextField maxJoueursField;

    // Panel équipes (EQUIPES / CAPTURE_DRAPEAU)
    @FXML private VBox    panelEquipes;
    @FXML private Spinner<Integer> spinnerEquipes;
    @FXML private Spinner<Integer> spinnerJoueursParEquipe;
    @FXML private VBox    nomsEquipesBox;
    @FXML private Label   infoJoueursLabel;

    private final SessionRepository     sessionRepository = new SessionRepository();
    private final BilletRepository      billetRepository  = new BilletRepository();
    private final QRCodeBilletRepository qrCodeRepository = new QRCodeBilletRepository();

    @FXML
    public void initialize() {
        // Type de partie
        typePartieCombo.getItems().setAll(
                TypePartie.ELIMINATION.name(),
                TypePartie.EQUIPES.name(),
                TypePartie.CAPTURE_DRAPEAU.name()
        );
        typePartieCombo.getSelectionModel().selectFirst();

        // Groupe toggle durée
        btn15.setToggleGroup(dureeGroup);
        btn30.setToggleGroup(dureeGroup);
        btn45.setToggleGroup(dureeGroup);
        btn60.setToggleGroup(dureeGroup);

        btn15.setUserData(15);
        btn30.setUserData(30);
        btn45.setUserData(45);
        btn60.setUserData(60);

        dureeGroup.selectedToggleProperty().addListener((obs, old, nw) -> {
            dureeSelectionnee = (nw != null) ? (int) nw.getUserData() : 0;
        });

        // Listeners spinners équipes → reconstruire champs noms
        spinnerEquipes.valueProperty().addListener((obs, old, nw) -> rafraichirNomsEquipes());
        spinnerJoueursParEquipe.valueProperty().addListener((obs, old, nw) -> mettreAJourInfoJoueurs());

        rafraichirNomsEquipes();
        mettreAJourInfoJoueurs();
    }

    @FXML
    public void onTypePartieChange() {
        String type = typePartieCombo.getValue();
        boolean equipes = "EQUIPES".equals(type) || "CAPTURE_DRAPEAU".equals(type);
        panelJoueursSimple.setVisible(!equipes);
        panelJoueursSimple.setManaged(!equipes);
        panelEquipes.setVisible(equipes);
        panelEquipes.setManaged(equipes);
    }

    @FXML
    public void onDureeSelect() {
        Toggle selected = dureeGroup.getSelectedToggle();
        dureeSelectionnee = (selected != null) ? (int) selected.getUserData() : 0;
        // Style visuel
        for (Toggle t : dureeGroup.getToggles()) {
            ToggleButton tb = (ToggleButton) t;
            if (t.isSelected()) {
                tb.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;");
            } else {
                tb.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;");
            }
        }
    }

    private void rafraichirNomsEquipes() {
        int nb = spinnerEquipes.getValue();
        // Conserver les valeurs saisies
        List<String> anciens = new ArrayList<>();
        for (var node : nomsEquipesBox.getChildren()) {
            if (node instanceof TextField tf) anciens.add(tf.getText());
        }
        nomsEquipesBox.getChildren().clear();
        for (int i = 0; i < nb; i++) {
            TextField tf = new TextField();
            tf.setPromptText("Nom équipe " + (i + 1));
            if (i < anciens.size()) tf.setText(anciens.get(i));
            nomsEquipesBox.getChildren().add(tf);
        }
        mettreAJourInfoJoueurs();
    }

    private void mettreAJourInfoJoueurs() {
        int eq  = spinnerEquipes.getValue();
        int jpq = spinnerJoueursParEquipe.getValue();
        int total = eq * jpq;
        infoJoueursLabel.setText("→ " + total + " joueur(s) au total — " + total + " billet(s) seront générés automatiquement.");
    }

    @FXML
    public void onCreer() {
        if (!validerFormulaire()) return;

        String     nom    = nomField.getText().trim();
        TypePartie type   = TypePartie.valueOf(typePartieCombo.getValue());
        LocalDate  expir  = dateExpirationPicker.getValue();
        boolean    equipes = type == TypePartie.EQUIPES || type == TypePartie.CAPTURE_DRAPEAU;

        int maxJoueurs;
        int nombreEquipes    = 0;
        int joueursParEquipe = 0;
        String nomsEquipes   = "";

        if (equipes) {
            nombreEquipes    = spinnerEquipes.getValue();
            joueursParEquipe = spinnerJoueursParEquipe.getValue();
            maxJoueurs       = nombreEquipes * joueursParEquipe;
            // Collecte noms
            List<String> noms = new ArrayList<>();
            for (var node : nomsEquipesBox.getChildren()) {
                if (node instanceof TextField tf) {
                    String n = tf.getText().trim();
                    noms.add(n.isEmpty() ? "Équipe " + (noms.size() + 1) : n);
                }
            }
            nomsEquipes = String.join(",", noms);
        } else {
            maxJoueurs = Integer.parseInt(maxJoueursField.getText().trim());
        }

        Session session = new Session(nom, type, dureeSelectionnee, maxJoueurs, StatutSession.OUVERTE);
        session.setDateExpiration(expir);
        session.setNombreEquipes(nombreEquipes);
        session.setJoueursParEquipe(joueursParEquipe);
        session.setNomsEquipes(nomsEquipes);

        final int billetsAGenerer = maxJoueurs;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                sessionRepository.save(session);
                // Générer automatiquement les billets
                for (int i = 0; i < billetsAGenerer; i++) {
                    Billet billet = new Billet(
                            UUID.randomUUID().toString(),
                            StatutBillet.VALIDE,
                            session.getDateExpiration(),
                            session
                    );
                    billetRepository.save(billet);
                    qrCodeRepository.genererEtSauvegarder(billet);
                }
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
        if (dureeSelectionnee <= 0) {
            afficherErreur("Veuillez sélectionner une durée de partie.");
            return false;
        }
        String type = typePartieCombo.getValue();
        boolean equipes = "EQUIPES".equals(type) || "CAPTURE_DRAPEAU".equals(type);
        if (!equipes) {
            try {
                int max = Integer.parseInt(maxJoueursField.getText().trim());
                if (max <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                afficherErreur("Le nombre de joueurs doit être un entier positif.");
                return false;
            }
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
    @FXML public void onNavBillets()  { NavigationHelper.navigateToBillets(); }
    @FXML public void onNavScanner()  { NavigationHelper.navigateTo("ScannerView.fxml"); }
}
