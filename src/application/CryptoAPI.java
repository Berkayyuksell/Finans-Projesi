package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CryptoAPI {
    private static final String ALL_PRICES_API_URL = "https://api.binance.com/api/v3/ticker/price";

    public static Map<String, Double> getAllPrices() {
        Map<String, Double> prices = new HashMap<>();
        try {
            URL url = new URL(ALL_PRICES_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonArray jsonArray = JsonParser.parseString(response.toString()).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject coin = jsonArray.get(i).getAsJsonObject();
                String symbol = coin.get("symbol").getAsString();
                if (symbol.endsWith("USDT")) {  
                    double price = coin.get("price").getAsDouble();
                    prices.put(symbol, price);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prices;
    }

    public static double getCryptoPrice(String symbol) {
        Map<String, Double> allPrices = getAllPrices();
        return allPrices.getOrDefault(symbol, -1.0);
    }
}
