package Controller;


import Model.Billet;
import Model.StatutBillet;
import Repository.BilletRepository;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class ScannerController {

    @FXML private TextField codeField;
    @FXML private VBox      resultOkBox;
    @FXML private VBox      resultErrBox;
    @FXML private Label     resultOkDetail;
    @FXML private Label     resultErrTitle;
    @FXML private Label     resultErrDetail;

    private final BilletRepository billetRepository = new BilletRepository();

    // ------------------------------------------------------------------ //
    //  Appelé automatiquement par JavaFX après le chargement du FXML      //
    // ------------------------------------------------------------------ //
    @FXML
    public void initialize() {
        codeField.requestFocus();
    }

    // ------------------------------------------------------------------ //
    //  Déclenché par le bouton "Valider" ou la touche Entrée (douchette)  //
    // ------------------------------------------------------------------ //
    @FXML
    public void onValider() {
        String code = codeField.getText().trim();
        if (code.isEmpty()) return;

        cacherResultats();

        // Accès BDD en arrière-plan — multithreading requis par le cahier des charges
        Task<Optional<Billet>> task = new Task<>() {
            @Override
            protected Optional<Billet> call() {
                return billetRepository.findByCode(code);
            }
        };

        task.setOnSucceeded(e -> {
            Optional<Billet> result = task.getValue();
            Platform.runLater(() -> afficherResultat(code, result));
        });

        task.setOnFailed(e -> Platform.runLater(() -> {
            afficherErreur("Erreur système", "Impossible de contacter la base de données.");
        }));

        new Thread(task).start();

        codeField.clear();
        codeField.requestFocus();
    }

    // ------------------------------------------------------------------ //
    //  Affichage du résultat selon le statut du billet                    //
    // ------------------------------------------------------------------ //
    private void afficherResultat(String code, Optional<Billet> optBillet) {
        if (optBillet.isEmpty()) {
            afficherErreur("Billet inconnu", "Aucun billet trouvé avec ce code.");
            return;
        }

        Billet billet = optBillet.get();

        switch (billet.getStatut()) {
            case VALIDE -> {
                // Marquer le billet comme utilisé en arrière-plan
                Task<Void> updateTask = new Task<>() {
                    @Override
                    protected Void call() {
                        billet.setStatut(StatutBillet.UTILISE);
                        billetRepository.update(billet);
                        return null;
                    }
                };
                new Thread(updateTask).start();

                resultOkDetail.setText(
                        "Code : " + code + " — " + billet.getSession().getNom()
                );
                resultOkBox.setVisible(true);
                resultOkBox.setManaged(true);
            }
            case UTILISE -> afficherErreur(
                    "Billet déjà utilisé",
                    "Ce billet a déjà été scanné à l'entrée."
            );
            case EXPIRE -> afficherErreur(
                    "Billet expiré",
                    "La date d'expiration de ce billet est dépassée."
            );
        }
    }

    // ------------------------------------------------------------------ //
    //  Navigation                                                          //
    // ------------------------------------------------------------------ //
    @FXML public void onNavSessions() { NavigationHelper.navigateTo("SessionListView.fxml"); }
    @FXML public void onNavBillets()  { NavigationHelper.navigateTo("BilletListView.fxml");  }
    @FXML public void onNavScanner()  { /* déjà sur cette vue */ }

    // ------------------------------------------------------------------ //
    //  Utilitaires privés                                                  //
    // ------------------------------------------------------------------ //
    private void cacherResultats() {
        resultOkBox.setVisible(false);
        resultOkBox.setManaged(false);
        resultErrBox.setVisible(false);
        resultErrBox.setManaged(false);
    }

    private void afficherErreur(String titre, String detail) {
        resultErrTitle.setText(titre);
        resultErrDetail.setText(detail);
        resultErrBox.setVisible(true);
        resultErrBox.setManaged(true);
    }
}