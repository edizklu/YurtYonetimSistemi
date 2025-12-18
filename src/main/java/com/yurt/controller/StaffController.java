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
import javafx.scene.chart.*;
import javafx.scene.layout.TilePane;

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

    @FXML private Label lblTotalStudent;
    @FXML private Label lblTotalRoom;
    @FXML private Label lblPendingRequests;
    @FXML private PieChart occupancyChart;
    @FXML private BarChart<String, Number> requestsChart;

    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> colStName;
    @FXML private TableColumn<Student, String> colStSurname;
    @FXML private TableColumn<Student, String> colStTc;
    @FXML private TableColumn<Student, Integer> colStRoom;

    @FXML private TextField stNameField;
    @FXML private TextField stSurnameField;
    @FXML private TextField stTcField;
    @FXML private TextField stPhoneField;
    @FXML private TextField stUsernameField;
    @FXML private PasswordField stPasswordField;
    @FXML private ComboBox<String> roomComboBox;

    @FXML private TilePane roomMapPane;


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

        loadAvailableRooms();
    }

    private void loadAvailableRooms() {
        roomComboBox.getItems().clear();
        String sql = "SELECT room_number FROM rooms WHERE current_count < capacity"; // Sadece boş yeri olanlar
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                roomComboBox.getItems().add(rs.getString("room_number"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadAllData() {
        loadDashboardData();
        loadPendingRequests();
        loadRooms();
        loadStudents();
        loadVisualMap();

    }

    private void loadDashboardData() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();

            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM students");
            if(rs1.next()) lblTotalStudent.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM rooms");
            if(rs2.next()) lblTotalRoom.setText(String.valueOf(rs2.getInt(1)));

            ResultSet rs3 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM requests WHERE status = 'Beklemede'");
            if(rs3.next()) lblPendingRequests.setText(String.valueOf(rs3.getInt(1)));

            ResultSet rsPie = conn.createStatement().executeQuery("SELECT SUM(capacity), SUM(current_count) FROM rooms");
            if (rsPie.next()) {
                int totalCap = rsPie.getInt(1);
                int used = rsPie.getInt(2);
                int empty = totalCap - used;

                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                        new PieChart.Data("Dolu (" + used + ")", used),
                        new PieChart.Data("Boş (" + empty + ")", empty)
                );
                occupancyChart.setData(pieData);
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Talepler");

            ResultSet rsBar = conn.createStatement().executeQuery("SELECT status, COUNT(*) FROM requests GROUP BY status");
            while (rsBar.next()) {
                String status = rsBar.getString(1);
                int count = rsBar.getInt(2);
                series.getData().add(new XYChart.Data<>(status, count));
            }
            requestsChart.getData().clear();
            requestsChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        String sql = "SELECT * FROM rooms ORDER BY room_number ASC";
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

    private void loadVisualMap() {
        if (roomMapPane == null) return; // Eğer sekme yüklenmediyse hata vermesin

        roomMapPane.getChildren().clear(); // Önce temizle

        String sql = "SELECT * FROM rooms ORDER BY room_number ASC";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String num = rs.getString("room_number");
                int cap = rs.getInt("capacity");
                int current = rs.getInt("current_count");

                Button roomBtn = new Button(num + "\n" + current + "/" + cap);
                roomBtn.setPrefSize(120, 100);

                String color = (current >= cap) ? "#e53935" : "#43a047";
                roomBtn.setStyle(
                        "-fx-background-color: " + color + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-background-radius: 10;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);"
                );

                roomBtn.setOnAction(e -> showRoomDetails(id, num));

                roomMapPane.getChildren().add(roomBtn);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefreshMap() {
        loadVisualMap();
        loadAvailableRooms();
    }

    @FXML
    private void handleDeleteRoom() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (selected.getCurrentCount() > 0) {
            showAlert("Hata", "İçinde öğrenci olan oda silinemez! Önce öğrencileri taşıyın veya silin.");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            conn.createStatement().executeUpdate("DELETE FROM rooms WHERE id = " + selected.getId());
            loadAllData();
            showAlert("Başarılı", "Oda silindi.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showRoomDetails(int roomId, String roomNum) {
        StringBuilder content = new StringBuilder();
        String sql = "SELECT name, surname FROM students WHERE room_id = ?";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();

            boolean hasStudent = false;
            while (rs.next()) {
                hasStudent = true;
                content.append("• ").append(rs.getString("name"))
                        .append(" ").append(rs.getString("surname")).append("\n");
            }

            if (!hasStudent) content.append("Bu oda şu an boş.");

        } catch (SQLException e) {
            e.printStackTrace();
            content.append("Veri çekilemedi.");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Oda Detayı");
        alert.setHeaderText(roomNum + " Nolu Oda Bilgisi");
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void loadStudents() {
        studentsList.clear();
        String sql = "SELECT * FROM students";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                int dbId = rs.getInt("id");
                String name = rs.getString("name");

                System.out.println("DEBUG: Listeye ekleniyor -> İsim: " + name + " | ID: " + dbId);

                studentsList.add(new Student(
                        dbId, "", "",
                        name, rs.getString("surname"),
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

    @FXML
    private void handleAddStudent() {
        // 1. Form Doğrulama
        if (stNameField.getText().isEmpty() || stUsernameField.getText().isEmpty() ||
                stPasswordField.getText().isEmpty() || roomComboBox.getValue() == null) {
            showAlert("Hata", "Lütfen tüm alanları doldurunuz ve bir oda seçiniz.");
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String roomNum = roomComboBox.getValue();
            int roomId = 0;
            PreparedStatement roomCheck = conn.prepareStatement("SELECT id, current_count, capacity FROM rooms WHERE room_number = ?");
            roomCheck.setString(1, roomNum);
            ResultSet rsRoom = roomCheck.executeQuery();

            if (rsRoom.next()) {
                if (rsRoom.getInt("current_count") >= rsRoom.getInt("capacity")) {
                    showAlert("Hata", "Seçilen oda dolmuştur!");
                    conn.rollback(); return;
                }
                roomId = rsRoom.getInt("id");
            }

            String userSql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'STUDENT')";
            PreparedStatement userStmt = conn.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, stUsernameField.getText());
            userStmt.setString(2, stPasswordField.getText());
            userStmt.executeUpdate();

            ResultSet rsUser = userStmt.getGeneratedKeys();
            int userId = 0;
            if (rsUser.next()) userId = rsUser.getInt(1);

            String stSql = "INSERT INTO students (user_id, name, surname, tc_no, phone, room_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stStmt = conn.prepareStatement(stSql);
            stStmt.setInt(1, userId);
            stStmt.setString(2, stNameField.getText());
            stStmt.setString(3, stSurnameField.getText());
            stStmt.setString(4, stTcField.getText());
            stStmt.setString(5, stPhoneField.getText());
            stStmt.setInt(6, roomId);
            stStmt.executeUpdate();

            String updateRoomSql = "UPDATE rooms SET current_count = current_count + 1 WHERE id = ?";
            PreparedStatement updateRoomStmt = conn.prepareStatement(updateRoomSql);
            updateRoomStmt.setInt(1, roomId);
            updateRoomStmt.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);

            showAlert("Başarılı", "Öğrenci başarıyla kaydedildi ve odaya yerleştirildi.");

            clearForm();
            loadStudents();
            loadDashboardData();
            loadRooms();
            loadAvailableRooms();

        } catch (SQLException e) {
            try { if(conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            showAlert("Hata", "Kayıt başarısız: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Silinecek öğrenciyi seçiniz.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Silme Onayı");
        alert.setContentText(selected.getName() + " silinecek. Emin misiniz?");
        if (alert.showAndWait().get() != ButtonType.OK) return;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String updateRoomSql = "UPDATE rooms SET current_count = current_count - 1 WHERE id = ?";
            PreparedStatement roomStmt = conn.prepareStatement(updateRoomSql);
            roomStmt.setInt(1, selected.getRoomId());
            roomStmt.executeUpdate();

            String delStSql = "DELETE FROM students WHERE id = ?";
            PreparedStatement delStStmt = conn.prepareStatement(delStSql);
            delStStmt.setInt(1, selected.getId());
            delStStmt.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);

            loadAllData();
            showAlert("Başarılı", "Öğrenci silindi ve oda kontenjanı güncellendi.");

        } catch (SQLException e) {
            try { if(conn!=null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMoveStudent() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            System.out.println("DEBUG: Seçilen Öğrenci ID: " + selectedStudent.getId());
        }

        String newRoomNum = roomComboBox.getValue();

        if (selectedStudent == null) {
            showAlert("Uyarı", "Lütfen listeden taşınacak öğrenciyi seçiniz.");
            return;
        }
        if (newRoomNum == null || newRoomNum.isEmpty()) {
            showAlert("Uyarı", "Lütfen sol taraftaki kutudan YENİ odayı seçiniz.");
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // TRANSACTION BAŞLAT

            // 1. YENİ ODAYI BUL VE KAPASİTE KONTROL ET
            int newRoomId = 0;
            PreparedStatement newRoomStmt = conn.prepareStatement("SELECT id, capacity, current_count FROM rooms WHERE room_number = ?");
            newRoomStmt.setString(1, newRoomNum);
            ResultSet rsNew = newRoomStmt.executeQuery();

            if (rsNew.next()) {
                if (rsNew.getInt("current_count") >= rsNew.getInt("capacity")) {
                    showAlert("Hata", "Seçilen yeni oda (" + newRoomNum + ") tamamen dolu!");
                    conn.rollback(); return;
                }
                newRoomId = rsNew.getInt("id");
            } else {
                showAlert("Hata", "Yeni oda bulunamadı.");
                conn.rollback(); return;
            }

            // Öğrenci zaten oradaysa dur
            if (selectedStudent.getRoomId() == newRoomId) {
                showAlert("Bilgi", "Öğrenci zaten bu odada.");
                conn.rollback(); return;
            }

            // 2. ÖĞRENCİYİ GÜNCELLE (Kritik Nokta: Önce öğrenciyi taşımayı dene)
            String updateStudentSql = "UPDATE students SET room_id = ? WHERE id = ?";
            PreparedStatement stStmt = conn.prepareStatement(updateStudentSql);
            stStmt.setInt(1, newRoomId);
            stStmt.setInt(2, selectedStudent.getId());

            int affectedRows = stStmt.executeUpdate(); // Güncellenen satır sayısı

            // EĞER HİÇBİR SATIR GÜNCELLENMEDİYSE (Öğrenci bulunamadıysa)
            if (affectedRows == 0) {
                throw new SQLException("Öğrenci veritabanında bulunamadı veya güncellenemedi. İşlem iptal ediliyor.");
            }

            // 3. ESKİ ODA SAYISINI DÜŞÜR
            String oldRoomSql = "UPDATE rooms SET current_count = current_count - 1 WHERE id = ?";
            PreparedStatement oldStmt = conn.prepareStatement(oldRoomSql);
            oldStmt.setInt(1, selectedStudent.getRoomId());
            oldStmt.executeUpdate();

            // 4. YENİ ODA SAYISINI ARTIR
            String newRoomUpdateSql = "UPDATE rooms SET current_count = current_count + 1 WHERE id = ?";
            PreparedStatement newStmt = conn.prepareStatement(newRoomUpdateSql);
            newStmt.setInt(1, newRoomId);
            newStmt.executeUpdate();

            // 5. HER ŞEY YOLUNDAYSA ONAYLA
            conn.commit();
            conn.setAutoCommit(true);

            showAlert("Başarılı", selectedStudent.getName() + " taşındı.");

            loadAllData();
            loadAvailableRooms();
            loadVisualMap();

        } catch (SQLException e) {
            try { if(conn!=null) conn.rollback(); } catch (SQLException ex) {} // Hata anında her şeyi geri al
            e.printStackTrace();
            showAlert("Kritik Hata", "İşlem başarısız: " + e.getMessage());
        }
    }

    private void clearForm() {
        stNameField.clear(); stSurnameField.clear(); stTcField.clear();
        stPhoneField.clear(); stUsernameField.clear(); stPasswordField.clear();
        roomComboBox.getSelectionModel().clearSelection();
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