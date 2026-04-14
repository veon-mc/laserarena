package com.laserarena.Controller;

import com.laserarena.Model.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationHelper {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    // Navigation simple vers une vue
    public static void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationHelper.class.getResource("/View/" + fxmlFile)
            );
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Navigation vers BilletListView avec injection de la session
    public static void navigateToWithSession(String fxmlFile, Session session) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationHelper.class.getResource("/View/" + fxmlFile)
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
