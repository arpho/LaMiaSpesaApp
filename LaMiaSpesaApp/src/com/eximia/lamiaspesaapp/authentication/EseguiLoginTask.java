package com.eximia.lamiaspesaapp.authentication;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.AsyncTask;

import com.eximia.lamiaspesaapp.utility.Util;

public class EseguiLoginTask extends AsyncTask<String, Void, String> {

	private String url;
	private Account lamiaspesaapp;
	private AccountManager mAccountManager;

	public String buildUrl() {
		// TODO Auto-generated method stub
		StringBuilder out = new StringBuilder(Util.getBaseUrl());
		out.append("/api_authentication");
		out.append("?email=");
		out.append(lamiaspesaapp.name);
		out.append("&password=");
		out.append(mAccountManager.getPassword(lamiaspesaapp));
		return out.toString();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Account getLamiaspesaapp() {
		return lamiaspesaapp;
	}

	public void setLamiaspesaapp(Account lamiaspesaapp) {
		this.lamiaspesaapp = lamiaspesaapp;
	}

	public AccountManager getmAccountManager() {
		return mAccountManager;
	}

	public void setmAccountManager(AccountManager mAccountManager) {
		this.mAccountManager = mAccountManager;
	}

	@Override
	protected String doInBackground(String... params) {
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		HttpPost loginPost = null;
		String token = null;
		String responseText;
		loginPost = new HttpPost(url);
		try {
			response = client.execute(loginPost);
			if (response.getStatusLine().getStatusCode() == 401) {
				// TODO o l'account non esiste o la password
				// è stata cambiata, deve avviare login attivity o
				// aggiornare le credenzialie questa setterà ilnuovo token
			} else {
				responseText = EntityUtils.toString(response.getEntity());
				try {
					token = new Util().getToken(responseText);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return token;
	}

}
