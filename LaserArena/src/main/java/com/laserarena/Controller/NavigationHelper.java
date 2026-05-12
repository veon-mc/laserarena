package com.laserarena.Controller;

import com.laserarena.Model.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class for navigating between the different views of the application.
 * <p>
 * Centralizes view-switching logic by replacing the root of the primary
 * {@link Stage}'s scene. All methods are static and require the primary stage
 * to have been initialized via {@link #setPrimaryStage(Stage)} at application startup.
 * </p>
 */
public class NavigationHelper {

    private static Stage primaryStage;

    /**
     * Sets the primary {@link Stage} of the application.
     * Must be called once at startup before any navigation.
     *
     * @param stage the primary stage of the application
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Navigates to a given FXML view by replacing the current scene root.
     *
     * @param fxmlFile the name of the FXML file to load (e.g. {@code "SessionListView.fxml"})
     */
    public static void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationHelper.class.getResource("/" + fxmlFile)
            );
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the ticket list view without a specific session.
     * <p>
     * Loads {@code BilletListView.fxml}, retrieves the associated
     * {@link BilletListController} and calls its {@link BilletListController#init()}
     * method to display all tickets.
     * </p>
     */
    public static void navigateToBillets() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationHelper.class.getResource("/BilletListView.fxml")
            );
            Parent root = loader.load();
            BilletListController controller = loader.getController();
            controller.init();
            primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the ticket list view filtered by a given session.
     * <p>
     * Loads the specified FXML file, retrieves the {@link BilletListController}
     * and injects the session via {@link BilletListController#setSession(Session)}
     * to display only that session's tickets.
     * </p>
     *
     * @param fxmlFile the name of the FXML file to load (e.g. {@code "BilletListView.fxml"})
     * @param session  the session whose tickets should be displayed
     */
    public static void navigateToWithSession(String fxmlFile, Session session) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationHelper.class.getResource("/" + fxmlFile)
            );
            Parent root = loader.load();
            BilletListController controller = loader.getController();
            controller.setSession(session);
            primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}