package com.yurt.controller;

import com.yurt.database.DatabaseConnection;
import com.yurt.model.LeaveRequest;
import com.yurt.model.Room;
import com.yurt.model.Staff;
import com.yurt.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffController {

    @FXML private Label welcomeLabel;

    @FXML private TableView<LeaveRequest> requestsTable;
    @FXML private TableColumn<LeaveRequest, Integer> colReqStudent;
    @FXML private TableColumn<LeaveRequest, String> colReqStart;
    @FXML private TableColumn<LeaveRequest, String> colReqEnd;
    @FXML private TableColumn<LeaveRequest, String> colReqReason;
    @FXML private TableColumn<LeaveRequest, String> colReqStatus;

    @FXML private TextField roomNumberField;
    @FXML private TextField capacityField;
    @FXML private TextField typeField;
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> colRoomNum;
    @FXML private TableColumn<Room, Integer> colCapacity;
    @FXML private TableColumn<Room, Integer> colCount;
    @FXML private TableColumn<Room, String> colType;

    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> colStName;
    @FXML private TableColumn<Student, String> colStSurname;
    @FXML private TableColumn<Student, String> colStTc;
    @FXML private TableColumn<Student, Integer> colStRoom;

    private Staff currentStaff;
    private ObservableList<LeaveRequest> pendingRequests = FXCollections.observableArrayList();
    private ObservableList<Room> roomsList = FXCollections.observableArrayList();
    private ObservableList<Student> studentsList = FXCollections.observableArrayList();

    public void setStaffData(Staff staff) {
        this.currentStaff = staff;
        welcomeLabel.setText("Yönetici: " + staff.getName());
        loadAllData();
    }

    @FXML
    public void initialize() {
        colReqStudent.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colReqStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colReqEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colReqReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colReqStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        requestsTable.setItems(pendingRequests);

        colRoomNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colCount.setCellValueFactory(new PropertyValueFactory<>("currentCount"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        roomsTable.setItems(roomsList);

        colStName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colStTc.setCellValueFactory(new PropertyValueFactory<>("tcNo"));
        colStRoom.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        studentsTable.setItems(studentsList);
    }

    private void loadAllData() {
        loadPendingRequests();
        loadRooms();
        loadStudents();
    }

    private void loadPendingRequests() {
        pendingRequests.clear();
        String sql = "SELECT * FROM requests WHERE status = 'Beklemede'";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                pendingRequests.add(new LeaveRequest(
                        rs.getInt("id"), rs.getInt("student_id"),
                        rs.getString("start_date"), rs.getString("end_date"),
                        rs.getString("reason"), rs.getString("status")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadRooms() {
        roomsList.clear();
        String sql = "SELECT * FROM rooms";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                roomsList.add(new Room(
                        rs.getInt("id"), rs.getString("room_number"),
                        rs.getInt("capacity"), rs.getInt("current_count"),
                        rs.getString("type")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadStudents() {
        studentsList.clear();
        String sql = "SELECT * FROM students";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                studentsList.add(new Student(
                        0, "", "",
                        rs.getString("name"), rs.getString("surname"),
                        rs.getString("tc_no"), rs.getInt("room_id")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleApprove() {
        processRequest("Onaylandı");
    }

    @FXML
    private void handleReject() {
        processRequest("Reddedildi");
    }

    private void processRequest(String newStatus) {
        LeaveRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen bir talep seçiniz.");
            return;
        }

        com.yurt.pattern.observer.NotificationService notifier = new com.yurt.pattern.observer.NotificationService();
        selected.attach(notifier);

        selected.setStatus(newStatus);

        String sql = "UPDATE requests SET status = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, selected.getId());
            stmt.executeUpdate();

            loadPendingRequests();
            showAlert("Bilgi", "Talep durumu güncellendi: " + newStatus);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAddRoom() {
        String num = roomNumberField.getText();
        String capStr = capacityField.getText();
        String type = typeField.getText();

        if (num.isEmpty() || capStr.isEmpty()) {
            showAlert("Hata", "Oda numarası ve kapasite zorunludur.");
            return;
        }

        try {
            int cap = Integer.parseInt(capStr);
            String sql = "INSERT INTO rooms (room_number, capacity, type) VALUES (?, ?, ?)";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, num);
            stmt.setInt(2, cap);
            stmt.setString(3, type);
            stmt.executeUpdate();

            roomNumberField.clear();
            capacityField.clear();
            typeField.clear();
            loadRooms();
            showAlert("Başarılı", "Yeni oda eklendi.");
        } catch (Exception e) {
            showAlert("Hata", "Oda eklenemedi: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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