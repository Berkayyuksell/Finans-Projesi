package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.MySQL.Util.VeritabaniUtil;

public class Portfolio {
    private Map<String, Double> portfolio;
    private Map<String, Double> purchasePrices;
    private int userId;

    public Portfolio(int userId) {
        this.userId = userId;
        portfolio = new HashMap<>();
        purchasePrices = new HashMap<>();
        loadPortfolioFromDatabase();
    }

    public void addAsset(String symbol, double amount, double purchasePrice) {
        portfolio.put(symbol, portfolio.getOrDefault(symbol, 0.0) + amount);
        purchasePrices.put(symbol, purchasePrice);
        saveAssetToDatabase(symbol, amount, purchasePrice);
    }

    public double getProfit(String symbol) {
        if (!portfolio.containsKey(symbol)) {
            return 0;
        }
        double currentPrice = getPrice(symbol);
        double purchasePrice = purchasePrices.get(symbol);
        return (currentPrice - purchasePrice) * portfolio.get(symbol);
    }

    public double getPurchasePrice(String symbol) {
        return purchasePrices.getOrDefault(symbol, 0.0);
    }

    public void sellAsset(String symbol, double amountToSell) {
        if (portfolio.containsKey(symbol)) {
            double currentAmount = portfolio.get(symbol);
            if (amountToSell >= currentAmount) {
                portfolio.remove(symbol);
                purchasePrices.remove(symbol);
                removeAssetFromDatabase(symbol);
            } else {
                portfolio.put(symbol, currentAmount - amountToSell);
                updateAssetInDatabase(symbol, currentAmount - amountToSell);
            }
        }
    }

    public Map<String, Double> getPortfolio() {
        return portfolio;
    }

    private void loadPortfolioFromDatabase() {
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
                portfolio.put(symbol, amount);
                purchasePrices.put(symbol, purchasePrice);
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

    private void saveAssetToDatabase(String symbol, double amount, double purchasePrice) {
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "REPLACE INTO portfolios (user_id, crypto_id, amount, purchase_price) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, symbol);
            preparedStatement.setDouble(3, amount);
            preparedStatement.setDouble(4, purchasePrice);
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

    private void updateAssetInDatabase(String symbol, double amount) {
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "UPDATE portfolios SET amount = ? WHERE user_id = ? AND crypto_id = ?";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setDouble(1, amount);
            preparedStatement.setInt(2, userId);
            preparedStatement.setString(3, symbol);
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

    private void removeAssetFromDatabase(String symbol) {
        Connection conn = VeritabaniUtil.Baglan();
        String sql = "DELETE FROM portfolios WHERE user_id = ? AND crypto_id = ?";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, symbol);
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

    double getPrice(String symbol) {
        return CryptoAPI.getCryptoPrice(symbol);
    }
}
