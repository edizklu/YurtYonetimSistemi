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
    @FXML private Label tcLabel;
    @FXML private Label roomLabel;
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
        welcomeLabel.setText("Hoşgeldin, " + student.getName());
        nameLabel.setText(student.getName() + " " + student.getSurname());
        tcLabel.setText("11*******");

        loadRoomInfo();
        loadLeaveRequests();
    }

    private void loadRoomInfo() {
        roomLabel.setText("Henüz atanmadı");
    }

    private void loadLeaveRequests() {
        requestList.clear();
        String sql = "SELECT * FROM requests WHERE student_id = ?";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, getStudentDbId());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requestList.add(new LeaveRequest(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("reason"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateRequest() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        String reason = reasonField.getText();

        if (start == null || end == null || reason.isEmpty()) {
            showAlert("Hata", "Lütfen tüm alanları doldurunuz.");
            return;
        }

        String sql = "INSERT INTO requests (student_id, start_date, end_date, reason, status) VALUES (?, ?, ?, ?, 'Beklemede')";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, getStudentDbId());
            stmt.setString(2, start.toString());
            stmt.setString(3, end.toString());
            stmt.setString(4, reason);
            stmt.executeUpdate();

            showAlert("Başarılı", "İzin talebiniz oluşturuldu!");

            startDate.setValue(null);
            endDate.setValue(null);
            reasonField.clear();
            loadLeaveRequests();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Hata", "Veritabanı hatası: " + e.getMessage());
        }
    }

    private int getStudentDbId() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM students WHERE user_id = ?");
        stmt.setInt(1, currentStudent.getId());
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getInt("id");
        return 0;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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