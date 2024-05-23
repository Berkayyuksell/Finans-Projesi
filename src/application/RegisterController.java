package application;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.MySQL.Util.VeritabaniUtil;

public class RegisterController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private PasswordField Password;

    @FXML
    private Button Register;

    @FXML
    private TextField Username;

    @FXML
    void RegisterClick(ActionEvent event) {
        String username = Username.getText();
        String password = Password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "HATA", "Kullanıcı adı ve Şifre Boş Kalamaz");
            return;
        }

        if (isUsernameTaken(username)) {
            showAlert(AlertType.ERROR, "HATA", "Kullanıcı Adı Alınmış");
            return;
        }

        if (registerUser(username, password)) {
            showAlert(AlertType.INFORMATION, "", "Başarılı şekilde kayıt oldunuz");
            closeWindow();
        } else {
            showAlert(AlertType.ERROR, "Hata", "Kayıt Başarısız Oldu");
        }
    }

    private boolean isUsernameTaken(String username) {
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "SELECT COUNT(*) FROM Login WHERE user = ?";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean registerUser(String username, String password) {
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "INSERT INTO Login (user, password, yetki) VALUES (?, ?, ?)";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setInt(3, 0);  
            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) Register.getScene().getWindow();
        stage.close();
    }

    @FXML
    void initialize() {
        assert Password != null : "fx:id=\"Password\" was not injected: check your FXML file 'Register.fxml'.";
        assert Register != null : "fx:id=\"Register\" was not injected: check your FXML file 'Register.fxml'.";
        assert Username != null : "fx:id=\"Username\" was not injected: check your FXML file 'Register.fxml'.";
    }
}
