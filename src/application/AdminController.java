package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

import com.MySQL.Util.VeritabaniUtil;

public class AdminController {

    @FXML
    private ListView<String> userList;

    @FXML
    private Button grantPermissionButton;

    @FXML
    private Button revokePermissionButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private Button logoutButton;

    @FXML
    private ListView<String> portfolioList;

    @FXML
    private Button viewTransactionHistoryButton;

    private ObservableList<String> users;
    private Map<String, Double> allPrices;

    private String adminUsername;

    public void initialize() {
        loadUsers();
        allPrices = CryptoAPI.getAllPrices();
    }

    public void setUsername(String username) {
        this.adminUsername = username;
    }

    private void loadUsers() {
        users = FXCollections.observableArrayList();
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "SELECT user, yetki FROM Login";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String user = resultSet.getString("user");
                int yetki = resultSet.getInt("yetki");
                users.add(user + " (Yetki: " + yetki + ")");
            }
            userList.setItems(users);
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
    }

    @FXML
    private void grantPermission(ActionEvent event) {
        String selectedUser = getSelectedUser();
        if (selectedUser != null) {
            updatePermission(selectedUser, 1);
        }
    }

    @FXML
    private void revokePermission(ActionEvent event) {
        String selectedUser = getSelectedUser();
        if (selectedUser != null) {
            updatePermission(selectedUser, 0);
        }
    }

    private void updatePermission(String username, int permission) {
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "UPDATE Login SET yetki = ? WHERE user = ?";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, permission);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
            loadUsers();
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
    }

   
    @FXML
    private void deleteUser(ActionEvent event) {
        String selectedUser = getSelectedUser();
        if (selectedUser != null) {
            Connection conn = VeritabaniUtil.Baglan();
            int userId = getUserId(selectedUser);
            if (userId != -1) {
                String deleteTransactionsSql = "DELETE FROM transactions WHERE user_id = ?";
                String deletePortfolioSql = "DELETE FROM portfolios WHERE user_id = ?";
                String deleteUserSql = "DELETE FROM Login WHERE user = ?";

                try {
                    conn.setAutoCommit(false);

                    try (PreparedStatement deleteTransactionsStmt = conn.prepareStatement(deleteTransactionsSql);
                         PreparedStatement deletePortfolioStmt = conn.prepareStatement(deletePortfolioSql);
                         PreparedStatement deleteUserStmt = conn.prepareStatement(deleteUserSql)) {

                        deleteTransactionsStmt.setInt(1, userId);
                        deleteTransactionsStmt.executeUpdate();

                        deletePortfolioStmt.setInt(1, userId);
                        deletePortfolioStmt.executeUpdate();

                        deleteUserStmt.setString(1, selectedUser);
                        deleteUserStmt.executeUpdate();

                        conn.commit();
                        loadUsers();
                    } catch (SQLException e) {
                        conn.rollback();
                        e.printStackTrace();
                    } finally {
                        conn.setAutoCommit(true);
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
            }
        }
    }


    @FXML
    private void viewPortfolio(ActionEvent event) {
        String selectedUser = getSelectedUser();
        if (selectedUser != null) {
            int userId = getUserId(selectedUser);
            if (userId != -1) {
                loadPortfolio(userId);
            }
        }
    }

    @FXML
    private void viewTransactionHistory(ActionEvent event) {
        String selectedUser = getSelectedUser();
        if (selectedUser != null) {
            int userId = getUserId(selectedUser);
            if (userId != -1) {
                loadTransactionHistory(userId);
            }
        }
    }

    @FXML
    private void logout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(new Scene(root));
            stage.show();
            Stage adminStage = (Stage) logoutButton.getScene().getWindow();
            adminStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the login window.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getSelectedUser() {
        String selectedItem = userList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            return selectedItem.split(" ")[0];
        }
        return null;
    }

    private int getUserId(String username) {
        int userId = -1;
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "SELECT KID FROM Login WHERE user = ?";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getInt("KID");
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
        return userId;
    }
    private void loadPortfolio(int userId) {
        ObservableList<String> portfolioItems = FXCollections.observableArrayList();
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "SELECT * FROM portfolios WHERE user_id = ?";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String symbol = resultSet.getString("crypto_id");
                double amount = resultSet.getDouble("amount");
                double purchasePrice = resultSet.getDouble("purchase_price");
                double currentPrice = allPrices.get(symbol);
                double profit = (currentPrice - purchasePrice) * amount;
                double percentageProfit = ((currentPrice - purchasePrice) / purchasePrice) * 100;
                portfolioItems.add(String.format("%s: %.2f Adet | Maliyet: $%.2f | Şuanki Fiyatı: $%.2f | Kar: $%.2f (%.2f%%)",
                        symbol, amount, purchasePrice, currentPrice, profit, percentageProfit));
            }
            portfolioList.setItems(portfolioItems);
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
    }

    private void loadTransactionHistory(int userId) {
        ObservableList<String> transactionItems = FXCollections.observableArrayList();
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "SELECT * FROM transactions WHERE user_id = ?";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String type = resultSet.getString("type");
                String symbol = resultSet.getString("symbol");
                double amount = resultSet.getDouble("amount");
                double price = resultSet.getDouble("price");
                Timestamp date = resultSet.getTimestamp("date");
                double volume = amount * price;

                transactionItems.add(String.format("%s - %s: %.2f adet $%.2f | Hacim: $%.2f | Tarih: %s",
                        type, symbol, amount, price, volume, date.toString()));
            }

            
            Stage transactionStage = new Stage();
            ListView<String> transactionListView = new ListView<>(transactionItems);
            transactionListView.setPrefSize(600, 400);

            Scene scene = new Scene(transactionListView);
            transactionStage.setScene(scene);
            transactionStage.setTitle("İşlem Geçmişi");
            transactionStage.show();
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
    }
}


    
