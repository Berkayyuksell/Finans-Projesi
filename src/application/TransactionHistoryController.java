package application;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.MySQL.Util.VeritabaniUtil;

public class TransactionHistoryController {

    @FXML
    private ListView<String> transactionListView;

    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
        loadTransactionHistory();
    }

    private void loadTransactionHistory() {
        ArrayList<String> transactions = new ArrayList<>();
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
                String date = resultSet.getString("date");
                double volume = amount * price; 
                transactions.add(String.format("%s - %s: %.2f adet $%.2f (Hacim: $%.2f) Tarih: %s", type, symbol, amount, price, volume, date));
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

        transactionListView.getItems().setAll(transactions);
    }
}
