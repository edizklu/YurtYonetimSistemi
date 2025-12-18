package com.yurt.controller;

import com.yurt.database.DatabaseConnection;
import com.yurt.pattern.UserFactory;
import com.yurt.model.User;
import com.yurt.model.Student;
import com.yurt.model.Staff;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int id = rs.getInt("id");

                User currentUser = UserFactory.createUser(role, id, user, pass);

                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Giriş Başarılı: " + role);
                System.out.println("DEBUG: Giriş başarılı, Rol: " + role);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                Parent root = null;

                if (currentUser instanceof Student) {
                    try {
                        System.out.println("DEBUG: Öğrenci ekranı yükleniyor...");
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/yurt/view/student_view.fxml"));
                        root = loader.load();

                        StudentController studentController = loader.getController();
                        studentController.setStudentData((Student) currentUser);

                    } catch (Exception e) {
                        System.err.println("!!! ÖĞRENCİ EKRANI HATASI !!!");
                        e.printStackTrace();
                        errorLabel.setText("Öğrenci ekranı açılamadı!");
                        return;
                    }

                } else if (currentUser instanceof Staff) {
                    try {
                        System.out.println("DEBUG: Personel ekranı yükleniyor...");
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/yurt/view/staff_view.fxml"));
                        root = loader.load();

                        StaffController staffController = loader.getController();
                        staffController.setStaffData((Staff) currentUser);

                    } catch (Exception e) {
                        System.err.println("!!! PERSONEL EKRANI HATASI !!!");
                        e.printStackTrace();
                        errorLabel.setText("Personel ekranı açılamadı!");
                        return;
                    }
                }

                if (root != null) {
                    stage.setScene(new Scene(root));
                    stage.centerOnScreen();
                }

            } else {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Hatalı Kullanıcı Adı veya Şifre");
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Sistem Hatası: " + e.getMessage());
        }
    }
}