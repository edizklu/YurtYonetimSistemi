package com.yurt.controller;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.LeaveRequest;
import com.yurt.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class StudentController {

    @FXML private Label welcomeLabel;
    @FXML private Label nameLabel;
    @FXML private Label surnameLabel;
    @FXML private Label tcLabel;
    @FXML private Label phoneLabel;

    @FXML private Label roomNumberLabel;
    @FXML private Label roomTypeLabel;
    @FXML private ListView<String> roommateList;

    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private TextField reasonField;
    @FXML private TableView<LeaveRequest> permissionsTable;
    @FXML private TableColumn<LeaveRequest, String> colStart;
    @FXML private TableColumn<LeaveRequest, String> colEnd;
    @FXML private TableColumn<LeaveRequest, String> colReason;
    @FXML private TableColumn<LeaveRequest, String> colStatus;

    private Student currentStudent;
    private int realStudentId;
    private ObservableList<LeaveRequest> requestList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        permissionsTable.setItems(requestList);
    }

    public void setStudentData(Student student) {
        this.currentStudent = student;

        loadStudentProfileByUserId(student.getId());
    }

    private void loadStudentProfileByUserId(int userId) {
        String sql = "SELECT s.id, s.name, s.surname, s.tc_no, s.phone, r.room_number, r.type, r.id as room_id " +
                "FROM students s " +
                "LEFT JOIN rooms r ON s.room_id = r.id " +
                "WHERE s.user_id = ?";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.realStudentId = rs.getInt("id");

                String name = rs.getString("name");
                String surname = rs.getString("surname");

                welcomeLabel.setText("Merhaba, " + name);
                nameLabel.setText(name);
                surnameLabel.setText(surname);
                tcLabel.setText(rs.getString("tc_no"));

                String phone = rs.getString("phone");
                phoneLabel.setText((phone != null && !phone.isEmpty()) ? phone : "-");

                String roomNum = rs.getString("room_number");
                if (roomNum != null) {
                    roomNumberLabel.setText(roomNum);
                    roomTypeLabel.setText(rs.getString("type"));

                    loadRoommates(rs.getInt("room_id"), this.realStudentId);
                } else {
                    roomNumberLabel.setText("Atanmadı");
                    roomTypeLabel.setText("-");
                    roommateList.getItems().clear();
                    roommateList.getItems().add("Henüz bir odaya yerleşmediniz.");
                }

                loadLeaveRequests();

            } else {
                welcomeLabel.setText("Profil Bulunamadı");
                nameLabel.setText("Hata: Öğrenci kaydı eksik.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRoommates(int roomId, int myStudentId) {
        roommateList.getItems().clear();
        String sql = "SELECT name, surname FROM students WHERE room_id = ? AND id != ?";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, roomId);
            stmt.setInt(2, myStudentId);
            ResultSet rs = stmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                roommateList.getItems().add(rs.getString("name") + " " + rs.getString("surname"));
            }

            if (!found) {
                roommateList.getItems().add("Bu odada şu an tek kalıyorsunuz.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadLeaveRequests() {
        requestList.clear();
        // user_id ile değil, gerçek student_id ile sorgu yapıyoruz
        String sql = "SELECT * FROM requests WHERE student_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.realStudentId); // DÜZELTİLDİ
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requestList.add(new LeaveRequest(
                        rs.getInt("id"), rs.getInt("student_id"),
                        rs.getString("start_date"), rs.getString("end_date"),
                        rs.getString("reason"), rs.getString("status")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleCreateRequest() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        String reason = reasonField.getText();

        if (start == null || end == null || reason.isEmpty()) {
            showAlert("Uyarı", "Lütfen tarih ve neden giriniz.");
            return;
        }

        String sql = "INSERT INTO requests (student_id, start_date, end_date, reason, status) VALUES (?, ?, ?, ?, 'Beklemede')";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.realStudentId); // DÜZELTİLDİ
            stmt.setString(2, start.toString());
            stmt.setString(3, end.toString());
            stmt.setString(4, reason);
            stmt.executeUpdate();

            showAlert("Başarılı", "İzin talebi gönderildi.");
            startDate.setValue(null); endDate.setValue(null); reasonField.clear();
            loadLeaveRequests();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() throws IOException {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/yurt/view/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        stage.setScene(scene);
    }
}