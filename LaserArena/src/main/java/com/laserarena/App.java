package com.laserarena;

import com.laserarena.Controller.NavigationHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        NavigationHelper.setPrimaryStage(primaryStage);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/SessionListView.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("LaserArena");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        try {
            java.sql.Connection conn = com.laserarena.Repository.DatabaseConnection.getInstance();
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}