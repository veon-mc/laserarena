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

/**
 * Controller for the session creation view.
 * <p>
 * Allows the operator to create a new lasergame session by filling in a form
 * with the session name, game type, duration, expiration date and player capacity.
 * Depending on the selected game type:
 * <ul>
 *     <li>{@link TypePartie#ELIMINATION}: the operator specifies a maximum number of players.</li>
 *     <li>{@link TypePartie#EQUIPES} or {@link TypePartie#CAPTURE_DRAPEAU}: the operator
 *         configures the number of teams, players per team and optional team names.</li>
 * </ul>
 * Upon creation, the session is saved to the database and the corresponding tickets
 * (with QR codes) are automatically generated asynchronously.
 * </p>
 */
public class SessionCreateController {

    @FXML private TextField         nomField;
    @FXML private ComboBox<String>  typePartieCombo;
    @FXML private DatePicker        dateExpirationPicker;
    @FXML private Label             errorLabel;

    @FXML private ToggleButton btn15;
    @FXML private ToggleButton btn30;
    @FXML private ToggleButton btn45;
    @FXML private ToggleButton btn60;
    private final ToggleGroup  dureeGroup = new ToggleGroup();
    private int dureeSelectionnee = 0;

    @FXML private VBox      panelJoueursSimple;
    @FXML private TextField maxJoueursField;

    @FXML private VBox    panelEquipes;
    @FXML private Spinner<Integer> spinnerEquipes;
    @FXML private Spinner<Integer> spinnerJoueursParEquipe;
    @FXML private VBox    nomsEquipesBox;
    @FXML private Label   infoJoueursLabel;

    private final SessionRepository     sessionRepository = new SessionRepository();
    private final BilletRepository      billetRepository  = new BilletRepository();
    private final QRCodeBilletRepository qrCodeRepository = new QRCodeBilletRepository();

    /**
     * FXML initialization method called automatically after the view is loaded.
     * <p>
     * Populates the game type combo box, configures the duration toggle buttons
     * into a {@link ToggleGroup}, and sets up listeners on the team spinners
     * to dynamically rebuild team name fields and update the player count label.
     * </p>
     */
    @FXML
    public void initialize() {
        typePartieCombo.getItems().setAll(
                TypePartie.ELIMINATION.name(),
                TypePartie.EQUIPES.name(),
                TypePartie.CAPTURE_DRAPEAU.name()
        );
        typePartieCombo.getSelectionModel().selectFirst();

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

        spinnerEquipes.valueProperty().addListener((obs, old, nw) -> reloadTeamNames());
        spinnerJoueursParEquipe.valueProperty().addListener((obs, old, nw) -> updateInfoPlayers());

        reloadTeamNames();
        updateInfoPlayers();
    }

    /**
     * Handles a change in the selected game type.
     * <p>
     * Toggles the visibility between the simple player count panel
     * (for {@link TypePartie#ELIMINATION}) and the team configuration panel
     * (for {@link TypePartie#EQUIPES} and {@link TypePartie#CAPTURE_DRAPEAU}).
     * </p>
     */
    @FXML
    public void onTypePartieChange() {
        String type = typePartieCombo.getValue();
        boolean equipes = "EQUIPES".equals(type) || "CAPTURE_DRAPEAU".equals(type);
        panelJoueursSimple.setVisible(!equipes);
        panelJoueursSimple.setManaged(!equipes);
        panelEquipes.setVisible(equipes);
        panelEquipes.setManaged(equipes);
    }

    /**
     * Handles the duration toggle button selection.
     * <p>
     * Updates the selected duration value and applies visual styling
     * to highlight the active button and reset the others.
     * </p>
     */
    @FXML
    public void onDureeSelect() {
        Toggle selected = dureeGroup.getSelectedToggle();
        dureeSelectionnee = (selected != null) ? (int) selected.getUserData() : 0;
        for (Toggle t : dureeGroup.getToggles()) {
            ToggleButton tb = (ToggleButton) t;
            if (t.isSelected()) {
                tb.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;");
            } else {
                tb.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;");
            }
        }
    }

    /**
     * Rebuilds the team name input fields based on the current number of teams.
     * <p>
     * Preserves previously entered team names when the spinner value changes.
     * Also triggers an update of the total player count label.
     * </p>
     */
    private void reloadTeamNames() {
        int nb = spinnerEquipes.getValue();
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
        updateInfoPlayers();
    }

    /**
     * Updates the information label showing the total number of players
     * and tickets that will be generated, based on the current team
     * and players-per-team spinner values.
     */
    private void updateInfoPlayers() {
        int eq  = spinnerEquipes.getValue();
        int jpq = spinnerJoueursParEquipe.getValue();
        int total = eq * jpq;
        infoJoueursLabel.setText("→ " + total + " joueur(s) au total — " + total + " billet(s) seront générés automatiquement.");
    }

    /**
     * Handles the session creation action triggered by the "Create" button.
     * <p>
     * Validates the form, builds a {@link Session} object from the input fields,
     * saves it to the database, then automatically generates the corresponding
     * {@link Billet} entries with their QR codes. All database operations are
     * performed asynchronously. On success, navigates back to the session list view.
     * </p>
     */
    @FXML
    public void onCreate() {
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
                for (int i = 0; i < billetsAGenerer; i++) {
                    Billet billet = new Billet(
                            UUID.randomUUID().toString(),
                            StatutBillet.VALIDE,
                            session.getDateExpiration(),
                            session
                    );
                    billetRepository.save(billet);
                    qrCodeRepository.generateAndSave(billet);
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() ->
                NavigationHelper.navigateTo("SessionListView.fxml")
        ));
        task.setOnFailed(e -> Platform.runLater(() ->
                showError("Erreur lors de la création de la session.")
        ));

        new Thread(task).start();
    }

    /**
     * Handles the cancel action by navigating back to the session list view
     * without saving.
     */
    @FXML
    public void onCancel() {
        NavigationHelper.navigateTo("SessionListView.fxml");
    }

    /**
     * Validates the session creation form.
     * <p>
     * Checks that the session name is not empty, a game type is selected,
     * a duration is chosen, the player count is a positive integer (for elimination mode),
     * and the expiration date is set and not in the past.
     * </p>
     *
     * @return {@code true} if all fields are valid, {@code false} otherwise
     */
    private boolean validerFormulaire() {
        errorLabel.setText("");

        if (nomField.getText().trim().isEmpty()) {
            showError("Le nom de la session est obligatoire.");
            return false;
        }
        if (typePartieCombo.getValue() == null) {
            showError("Veuillez sélectionner un type de partie.");
            return false;
        }
        if (dureeSelectionnee <= 0) {
            showError("Veuillez sélectionner une durée de partie.");
            return false;
        }
        String type = typePartieCombo.getValue();
        boolean equipes = "EQUIPES".equals(type) || "CAPTURE_DRAPEAU".equals(type);
        if (!equipes) {
            try {
                int max = Integer.parseInt(maxJoueursField.getText().trim());
                if (max <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showError("Le nombre de joueurs doit être un entier positif.");
                return false;
            }
        }
        if (dateExpirationPicker.getValue() == null) {
            showError("Veuillez sélectionner une date d'expiration.");
            return false;
        }
        if (dateExpirationPicker.getValue().isBefore(LocalDate.now())) {
            showError("La date d'expiration ne peut pas être dans le passé.");
            return false;
        }
        return true;
    }

    /**
     * Displays an error message in the error label.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
    }

    /**
     * Navigates to the session list view.
     */
    @FXML public void onNavSessions() { NavigationHelper.navigateTo("SessionListView.fxml"); }

    /**
     * Navigates to the ticket list view showing all tickets.
     */
    @FXML public void onNavBillets()  { NavigationHelper.navigateToBillets(); }

    /**
     * Navigates to the ticket scanner view.
     */
    @FXML public void onNavScanner()  { NavigationHelper.navigateTo("ScannerView.fxml"); }
}