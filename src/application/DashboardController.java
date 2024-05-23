package application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Map;

import com.MySQL.Util.VeritabaniUtil;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<String> portfolioListView;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> searchResultsListView;

    @FXML
    private TextField amountField;

    @FXML
    private Label profitLabel;

    @FXML
    private Label totalBalanceLabel;

    @FXML
    private Button addButton;

    @FXML
    private Button sellButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button transactionHistoryButton;

    @FXML
    private Slider sellPercentageSlider;

    private Portfolio portfolio;
    private Map<String, Double> allPrices;
    private int userId;

    public void initialize() {
        allPrices = CryptoAPI.getAllPrices();
        updateSearchResultsList("");  
        startPriceUpdateThread();
    }

    public void setUsername(String username) {
        welcomeLabel.setText("Hoşgeldiniz, " + username + "!");
        userId = getUserId(username);
        portfolio = new Portfolio(userId);
        updatePortfolioList();
    }

    private int getUserId(String username) {
        int userId = -1;
        try (Connection conn = VeritabaniUtil.Baglan();
             PreparedStatement preparedStatement = conn.prepareStatement("SELECT KID FROM Login WHERE user = ?")) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getInt("KID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    @FXML
    private void onSearch(KeyEvent event) {
        String searchText = searchField.getText().toUpperCase();
        updateSearchResultsList(searchText);
    }

    private void updateSearchResultsList(String searchText) {
        ObservableList<String> results = FXCollections.observableArrayList();
        for (String symbol : allPrices.keySet()) {
            if (symbol.contains(searchText) && symbol.endsWith("USDT")) {
                results.add(symbol + " - $" + String.format("%.2f", allPrices.get(symbol)));  
            }
        }
        searchResultsListView.setItems(results);
    }

    @FXML
    private void addCrypto() {
        String selectedCrypto = searchResultsListView.getSelectionModel().getSelectedItem();
        if (selectedCrypto != null) {
            String[] parts = selectedCrypto.split(" - \\$");
            String symbol = parts[0];
            double amount = Double.parseDouble(amountField.getText());
            double price = Double.parseDouble(parts[1].replace(",", ".")); 
            portfolio.addAsset(symbol, amount, price);
            recordTransaction("buy", symbol, amount, price);
            updatePortfolioList();
            showAlert("İşlem Başarılı", String.format("%s - %.2f adet $%.2f ile eklendi.", symbol, amount, amount * price));
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        
    }

    @FXML
    private void sellAsset() {
        String selectedAsset = portfolioListView.getSelectionModel().getSelectedItem();
        if (selectedAsset != null) {
            String symbol = selectedAsset.split(":")[0].trim();
            double totalAmount = portfolio.getPortfolio().get(symbol);
            double sellPercentage = sellPercentageSlider.getValue() / 100.0;
            double amountToSell = totalAmount * sellPercentage;
            double price = CryptoAPI.getCryptoPrice(symbol);
            showAlert("İşlem Başarılı", String.format("%s - %.2f adet $%.2f ile satıldı.", symbol, amountToSell, amountToSell * price));
            if (amountToSell > 0) {
                portfolio.sellAsset(symbol, amountToSell);  
                recordTransaction("sell", symbol, amountToSell, price);
                updatePortfolioList();
            }
        }
        
    }

    private void startPriceUpdateThread() {
        new Thread(() -> {
            while (true) {
                Platform.runLater(this::updatePortfolioList);
                try {
                    Thread.sleep(5000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updatePortfolioList() {
        portfolioListView.getItems().clear();
        Map<String, Double> portfolioData = portfolio.getPortfolio();
        double totalBalance = 0;
        double totalProfit = 0;
        for (String symbol : portfolioData.keySet()) {
            double currentPrice = CryptoAPI.getCryptoPrice(symbol);
            double purchasePrice = portfolio.getPurchasePrice(symbol);
            double amount = portfolioData.get(symbol);
            double profit = (currentPrice - purchasePrice) * amount;
            double percentageProfit = ((currentPrice - purchasePrice) / purchasePrice) * 100;
            totalBalance += currentPrice * amount;
            totalProfit += profit;
            double volume = amount * currentPrice;
            portfolioListView.getItems().add(
                String.format("%s: %.2f Adet | Maliyet: $%.2f | Mevcut Fiyatı: $%.2f | Kar/zarar: $%.2f (%.2f%%) bakiye : $%.2f ",
                symbol, amount, purchasePrice, currentPrice, profit, percentageProfit ,volume)
            );
        }
        totalBalanceLabel.setText("Bakiye: " + String.format("%.2f", totalBalance));
        profitLabel.setText("Kar/zarar: " + String.format("%.2f", totalProfit));
    }

    private void recordTransaction(String type, String symbol, double amount, double price) {
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "INSERT INTO transactions (user_id, type, symbol, amount, price) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, symbol);
            preparedStatement.setDouble(4, amount);
            preparedStatement.setDouble(5, price);
            preparedStatement.executeUpdate();
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
    private void showTransactionHistory() {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("transaction_history.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            TransactionHistoryController controller = fxmlLoader.getController();
            controller.setUserId(userId);
            stage.setTitle("Transaction History");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.close();
            Stage newStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

