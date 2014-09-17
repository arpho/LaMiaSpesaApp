package com.eximia.lamiaspesaapp.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.eximia.lamiaspesaapp.authentication.AccountGeneral;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.net.ParseException;
import android.util.Log;

/**
 * Created by arpho on 15/08/14.
 */
public class Util {
	final private String IP = this.getProperty("SERVER_IP");

	public String getBaseUrl() {
		// TODO deve ottenere l'indirizzo da un file di properties
		return "http://" + IP + ":8080";// "http://192.168.1.66:8080";//
										// "http://10.141.158.1:8080";
										// 10.141.158.1
		// TODO ottenere l'indirizzo da un file di properties

	}

	private Account getEximiaAccount(AccountManager mAccountManager) {
		Account[] accounts = mAccountManager.getAccounts();
		Account out = null;
		int i = 0;
		while (i < accounts.length) {
			if (accounts[i].type.equalsIgnoreCase("com.eximia.lamiaspesaapp"))
				out = accounts[i];
			i += 1;
		}
		return out;
	}

	public void setToken(String token, AccountManager mAccountManager) {
		Account lamiaspesaapp = getEximiaAccount(mAccountManager);
		if (token != null)
			mAccountManager.setAuthToken(lamiaspesaapp,
					AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, token);
	}

	public String getProperty(String key) {
		Properties props = new Properties();
		// new File("res/raw/textfile.txt")
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("res/raw/lamiaspesaapp.properties");
		try {
			props.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return props.getProperty(key);
	}

	public String estraiTokenMyWay(HttpResponse itemResponse)
			throws JSONException {
		StringBuilder itemBuilder = new StringBuilder();

		HttpEntity itemEntity = itemResponse.getEntity();
		InputStream productContent = null;
		try {
			productContent = itemEntity.getContent();
			InputStreamReader itemInput = new InputStreamReader(productContent);
			BufferedReader itemReader = new BufferedReader(itemInput);
			String lineIn;
			while ((lineIn = itemReader.readLine()) != null) {
				itemBuilder.append(lineIn);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject resultObject = null;
		try {
			resultObject = new JSONObject(itemBuilder.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resultObject.getString("sessionToken");
	}

	public String getToken(String response) throws JSONException {
		String token = "";
		Log.d("eximia", "risposta token: " + response);
		JSONObject jObject = new JSONObject(response);
		token = (String) jObject.get("sesionToken");
		Log.d("eximia", "token: " + token);
		return token;
	}

	public String estraiToken(HttpResponse response)
			throws UnsupportedEncodingException, IllegalStateException,
			IOException, JSONException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent(), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String json = reader.readLine();
		JSONTokener tokener = new JSONTokener(json);
		JSONArray finalResult = new JSONArray(tokener);
		return finalResult.getString(0);

	}

	public String normalizzaUpc(String upc) {
		while (upc.length() < 13) {
			upc = upc + '0';
		}

		return upc;
	}
}
