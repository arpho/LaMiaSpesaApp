package com.eximia.lamiaspesaapp.authentication;

import android.util.Log;

import com.eximia.lamiaspesaapp.utility.Util;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Handles the comminication with Parse.com
 * 
 * User: udinic Date: 3/27/13 Time: 3:30 AM
 */
public class ParseComServerAuthenticate implements ServerAuthenticate {
	@Override
	public String userSignUp(String name, String email, String pass,
			String authType) throws Exception {

		String url = "https://api.parse.com/1/users";

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);

		httpPost.addHeader("X-Parse-Application-Id",
				"XUafJTkPikD5XN5HxciweVuSe12gDgk2tzMltOhr");
		httpPost.addHeader("X-Parse-REST-API-Key",
				"8L9yTQ3M86O4iiucwWb4JS7HkxoSKo7ssJqGChWx");
		httpPost.addHeader("Content-Type", "application/json");

		String user = "{\"username\":\"" + email + "\",\"password\":\"" + pass
				+ "\",\"phone\":\"415-392-0202\"}";
		HttpEntity entity = new StringEntity(user);
		httpPost.setEntity(entity);

		String authtoken = null;
		try {
			HttpResponse response = httpClient.execute(httpPost);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() != 201) {
				ParseComError error = new Gson().fromJson(responseString,
						ParseComError.class);
				throw new Exception("Error creating user[" + error.code
						+ "] - " + error.error);
			}

			User createdUser = new Gson().fromJson(responseString, User.class);

			authtoken = createdUser.sessionToken;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return authtoken;
	}

	@Override
	public String userSignIn(String user, String pass, String authType)
			throws Exception {

		Log.d("eximia", "userSignIn");

		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = Util.getBaseUrl() + "/api_authentication";
		// TODO impostare per il mio server
		// authType è il tipo di autenticazione richiesta, a me non interessa

		String query = null;
		try {
			query = String.format("%s=%s&%s=%s", "email",
					URLEncoder.encode(user, "UTF-8"), "password", pass);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		url += "?" + query;
		Log.d("eximia", url);

		HttpPost httpPost = new HttpPost(url);

		httpPost.addHeader("X-Parse-Application-Id",
				"XUafJTkPikD5XN5HxciweVuSe12gDgk2tzMltOhr");
		httpPost.addHeader("X-Parse-REST-API-Key",
				"8L9yTQ3M86O4iiucwWb4JS7HkxoSKo7ssJqGChWx");

		HttpParams params = new BasicHttpParams();
		params.setParameter("email", user);
		params.setParameter("password", pass);
		httpPost.setParams(params);
		// httpGet.getParams().setParameter("username",
		// user).setParameter("password", pass);

		String authtoken = null;
		StringBuilder itemBuilder = new StringBuilder();
		try {
			HttpResponse response = httpClient.execute(httpPost);
			//String responseString = EntityUtils.toString(response.getEntity());
			if (response.getStatusLine().getStatusCode() == 401)
				throw new Exception("not authorized");
			if (response.getStatusLine().getStatusCode() == 200) {
				String responseText = EntityUtils.toString(response.getEntity()); 
				authtoken = new Util().getToken(responseText);
				Log.d("eximia",authtoken);
				/*
				 * throw new Exception("Error signing-in [" + error.code +
				 * "] - " + error.error);
				 */
			}

			// User loggedUser = new Gson().fromJson(response.toString(),
			// User.class);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return authtoken;
	}

	private class ParseComError implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int code;
		String error;
	}

	private class User implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String firstName;
		private String lastName;
		private String username;
		private String phone;
		private String objectId;
		public String sessionToken;
		private String gravatarId;
		private String avatarUrl;

		@SuppressWarnings("unused")
		public String getFirstName() {
			return firstName;
		}

		@SuppressWarnings("unused")
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		@SuppressWarnings("unused")
		public String getLastName() {
			return lastName;
		}

		@SuppressWarnings("unused")
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		@SuppressWarnings("unused")
		public String getUsername() {
			return username;
		}

		@SuppressWarnings("unused")
		public void setUsername(String username) {
			this.username = username;
		}

		@SuppressWarnings("unused")
		public String getPhone() {
			return phone;
		}

		@SuppressWarnings("unused")
		public void setPhone(String phone) {
			this.phone = phone;
		}

		@SuppressWarnings("unused")
		public String getObjectId() {
			return objectId;
		}

		@SuppressWarnings("unused")
		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}

		@SuppressWarnings("unused")
		public String getSessionToken() {
			return sessionToken;
		}

		@SuppressWarnings("unused")
		public void setSessionToken(String sessionToken) {
			this.sessionToken = sessionToken;
		}

		@SuppressWarnings("unused")
		public String getGravatarId() {
			return gravatarId;
		}

		@SuppressWarnings("unused")
		public void setGravatarId(String gravatarId) {
			this.gravatarId = gravatarId;
		}

		@SuppressWarnings("unused")
		public String getAvatarUrl() {
			return avatarUrl;
		}

		@SuppressWarnings("unused")
		public void setAvatarUrl(String avatarUrl) {
			this.avatarUrl = avatarUrl;
		}
	}
}
