package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableDataModelWithStatus {
	private StringProperty market;
	private StringProperty coin;
	private StringProperty price;
	private StringProperty lowerValue;
	private StringProperty upperValue;
	
	public TableDataModelWithStatus(StringProperty market, StringProperty coin, StringProperty lowerValue, StringProperty upperValue) {
		this.market = market;
		this.coin = coin;
		this.price = getCurrentPrice(market,coin);
		this.lowerValue = lowerValue;
		this.upperValue = upperValue;
		
	}

	
	public StringProperty coinProperty() {
		return new SimpleStringProperty(market.get() +" " +coin.get());
	}
	
	public StringProperty priceProperty() {
		this.price = getCurrentPrice(market, coin);
		return price;
	}
	
	
	public StringProperty lowerValueProperty() {
		return lowerValue;
	}
	
	public StringProperty upperValueProperty() {
		return upperValue;
	}
	
	
	public StringProperty getCurrentPrice(StringProperty market, StringProperty coin) {

		String htt = "https://crix-api-endpoint.upbit.com/v1/crix/candles/minutes/1?code=CRIX.UPBIT." +market.get() +"-" + coin.get() + "&count=1";

		//HttpURLConnection http = (HttpURLConnection)url.openConnection();
		//int statusCode = http.getResponseCode();

		StringBuilder result = new StringBuilder();
		URL url;
		try {
			url = new URL(htt);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
			// System.out.println(result.toString());
			if (result.length() > 2) {
			
			
			JSONParser jsonParser = new JSONParser();

			JSONArray jsonObj = (JSONArray) jsonParser.parse(result.toString());

			JSONObject j = (JSONObject) jsonObj.get(0);
			
			DecimalFormat df = new DecimalFormat("#,##0");
			double n = Double.valueOf(j.get("tradePrice").toString()).doubleValue();
			
			if (market.get().equals("KRW")) {
				df = new DecimalFormat("#,##0");
			}else if (market.get().equals("BTC") || market.get().equals("ETH")) {
				df = new DecimalFormat(".########");
			}
			

			return new SimpleStringProperty(String.valueOf(df.format(n)));

			}else {
				return new SimpleStringProperty("ERROR");
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new SimpleStringProperty("ERROR");


	}
	
}
