package com.yurt.main;

import com.yurt.database.DatabaseConnection;
import com.yurt.database.DataSeeder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseConnection.getInstance();
        DataSeeder.seedData();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/yurt/view/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);

        stage.setTitle("Yurt Yönetim Sistemi");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}