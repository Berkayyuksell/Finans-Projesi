package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

import com.MySQL.Util.VeritabaniUtil;

public class SampleController {

    @FXML
    private Button LoginButton;

    @FXML
    private PasswordField PasswordText;

    @FXML
    private Button RegisterButton;

    @FXML
    private TextField UsernameText;

    @FXML
    private Label label;

    @FXML
    void LoginButtonClick(ActionEvent event) {
        String username = UsernameText.getText();
        String password = PasswordText.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Hata", "Kullanıcı adı ve şifre kısmı boş bırakılamaz!");
            return;
        }

        int userPermission = checkLogin(username, password);
        if (userPermission >= 0) {
            showAlert("", "Giriş Başarılı!");
            openDashboard(username, userPermission);
        } else {
            showAlert("Hata", "kullanıcı adı veya parola yanlış!");
        }
    }

    @FXML
    void RegisterButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Register.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "");
        }
    }

    private int checkLogin(String username, String password) {
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "SELECT yetki FROM Login WHERE user = ? AND password = ?";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("yetki");  
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return -1;  
    }

    private void openDashboard(String username, int userPermission) {
        try {
            String fxmlFile = (userPermission == 1) ? "admin.fxml" : "dashboard.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();

            if (userPermission == 1) {
                
                AdminController adminController = fxmlLoader.getController();
               	
            } else {
                
                DashboardController dashboardController = fxmlLoader.getController();
                dashboardController.setUsername(username);
            }

            Stage stage = new Stage();
            stage.setTitle((userPermission == 1) ? "Admin Dashboard" : "Dashboard");
            stage.setScene(new Scene(root));
            stage.show();

            
            Stage loginStage = (Stage) LoginButton.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Hata", "");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
