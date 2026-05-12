package com.laserarena.Controller;

import com.laserarena.Model.Billet;
import com.laserarena.Model.StatutBillet;
import com.laserarena.Repository.BilletRepository;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Controller for the Scanner view, responsible for validating tickets at the arena entrance.
 * <p>
 * Allows the operator to enter or scan (via barcode reader) a ticket code,
 * then checks its status in the database:
 * <ul>
 *     <li>{@link StatutBillet#VALIDE}: the ticket is accepted and its status is updated to {@link StatutBillet#UTILISE}.</li>
 *     <li>{@link StatutBillet#UTILISE}: an error message indicates the ticket has already been scanned.</li>
 *     <li>{@link StatutBillet#EXPIRE}: an error message indicates the ticket has expired.</li>
 * </ul>
 * If the code does not match any ticket, an "unknown ticket" error message is displayed.
 * All database operations are performed asynchronously.
 * </p>
 */
public class ScannerController {

    @FXML private TextField codeField;
    @FXML private VBox      resultOkBox;
    @FXML private VBox      resultErrBox;
    @FXML private Label     resultOkDetail;
    @FXML private Label     resultErrTitle;
    @FXML private Label     resultErrDetail;

    private final BilletRepository billetRepository = new BilletRepository();

    /**
     * FXML initialization method called automatically after the view is loaded.
     * Sets the focus on the code input field to allow immediate scanning.
     */
    @FXML
    public void initialize() {
        codeField.requestFocus();
    }

    /**
     * Handles the ticket code validation action, triggered by the "Valider" button
     * or the Enter key (sent automatically by the barcode reader).
     * <p>
     * The entered code is trimmed and sanitized (the {@code §} character is replaced
     * by {@code -}). An asynchronous database lookup is then performed to retrieve
     * the corresponding ticket. The result is displayed via {@link #showResult(String, Optional)}.
     * The input field is cleared and refocused after each scan.
     * </p>
     */
    @FXML
    public void onValidate() {
        String code = codeField.getText().trim();
        code = code.replace("§", "-");
        if (code.isEmpty()) return;

        HideResults();

        String finalCode = code;
        Task<Optional<Billet>> task = new Task<>() {
            @Override
            protected Optional<Billet> call() {
                return billetRepository.findByCode(finalCode);
            }
        };

        String finalCode1 = code;
        task.setOnSucceeded(e -> {
            Optional<Billet> result = task.getValue();
            Platform.runLater(() -> showResult(finalCode1, result));
        });

        task.setOnFailed(e -> Platform.runLater(() -> {
            ShowError("Erreur système", "Impossible de contacter la base de données.");
        }));

        new Thread(task).start();

        codeField.clear();
        codeField.requestFocus();
    }

    /**
     * Displays the validation result based on the ticket status.
     * <p>
     * If the ticket is {@link StatutBillet#VALIDE}, it is marked as {@link StatutBillet#UTILISE}
     * asynchronously and a success message is shown. For tickets that are already used or expired,
     * an appropriate error message is displayed. If no ticket matches the scanned code,
     * an "unknown ticket" error is shown.
     * </p>
     *
     * @param code      the scanned ticket code
     * @param optBillet an {@link Optional} containing the ticket if found, or empty otherwise
     */
    private void showResult(String code, Optional<Billet> optBillet) {
        if (optBillet.isEmpty()) {
            ShowError("Billet inconnu", "Aucun billet trouvé avec ce code.");
            return;
        }

        Billet billet = optBillet.get();

        switch (billet.getStatut()) {
            case VALIDE -> {
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
            case UTILISE -> ShowError(
                    "Billet déjà utilisé",
                    "Ce billet a déjà été scanné à l'entrée."
            );
            case EXPIRE -> ShowError(
                    "Billet expiré",
                    "La date d'expiration de ce billet est dépassée."
            );
        }
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
     * Navigation action for the scanner view (no-op since this is the current view).
     */
    @FXML public void onNavScanner()  { /* already on this view */ }

    /**
     * Hides both the success and error result boxes.
     */
    private void HideResults() {
        resultOkBox.setVisible(false);
        resultOkBox.setManaged(false);
        resultErrBox.setVisible(false);
        resultErrBox.setManaged(false);
    }

    /**
     * Displays an error message in the error result box.
     *
     * @param titre  the error title
     * @param detail the error detail message
     */
    private void ShowError(String titre, String detail) {
        resultErrTitle.setText(titre);
        resultErrDetail.setText(detail);
        resultErrBox.setVisible(true);
        resultErrBox.setManaged(true);
    }
}