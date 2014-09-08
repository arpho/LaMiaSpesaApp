package com.eximia.lamiaspesaapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.eximia.lamiaspesaapp.authentication.AccountGeneral;
import com.eximia.lamiaspesaapp.authentication.AuthenticatorActivity;
import com.eximia.lamiaspesaapp.authentication.EximiaAuthenticator;
import com.eximia.lamiaspesaapp.utility.SingleBundle;
import com.eximia.lamiaspesaapp.utility.Util;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanActivity extends FragmentActivity implements
		View.OnClickListener, ScanFragment.OnFragmentInteractionListener,
		ProductFragment.OnFragmentInteractionListener {
	private static final String TAG = ScanActivity.class.getSimpleName();
	private final Boolean sviluppo = true;
	private Bundle scanBundle;
	private Boolean serverRaggiungibile = true;
	public final static String PARAM_USER_PASS = "USER_PASS";
	private String scanContent4postlogin;

	public Bundle preparaBundle(JSONObject data) throws JSONException {
		Bundle bundle = new Bundle();
		bundle.putBoolean("hasResult", true);
		bundle.putString("format", "json");
		bundle.putString("nome_prodotto", data.getString("itemname"));
		bundle.putString("descrizione", data.getString("description"));
		Double ratingsUp = Double.valueOf(data.getString("ratingsup"));
		Double ratingsdown = Double.valueOf(data.getString("ratingsdown"));
		Double rate = ratingsUp / (ratingsUp + ratingsdown) * 5;
		bundle.putDouble("rate", rate);
		bundle.putString("picture", data.getString("pictures"));
		bundle.putString("developing", sviluppo ? "modalità sviluppo" : "");

		return bundle;
	}

	private Bundle preparaBundle(String format, String content,
			String prodotto, String descrizione, double rate, String picture) {
		// TODO da modificare, deve prendere il json fornito dal server
		Bundle bundle = new Bundle();
		bundle.putBoolean("hasResult", true);
		bundle.putString("content", content);
		bundle.putString("format", format);
		bundle.putString("prodotto", prodotto);
		bundle.putString("descrizione", descrizione);
		bundle.putDouble("rate", rate);
		bundle.putString("picture", picture);
		bundle.putString("developing", sviluppo ? "modalità sviluppo" : "");

		return bundle;
	}

	private Bundle preparaBundleNoResult() {
		// TODO da modificare, deve prendere il json fornito dal server
		Bundle bundle = new Bundle();
		bundle.putBoolean("hasResult", false);
		bundle.putString("developing", sviluppo ? "modalità sviluppo" : "");

		return bundle;
	}

	private void switch2ProductFragment(JSONObject data) {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		android.support.v4.app.FragmentTransaction transaction;
		transaction = fm.beginTransaction();
		Bundle bundle = null;
		Log.d(TAG, "onPostExecute ha invocato switch2ProductFragment");
		try {
			bundle = preparaBundle(data);
		} catch (JSONException e) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"dato non previsto", Toast.LENGTH_SHORT);
			toast.show();
			e.printStackTrace();
		}
		ProductFragment productFragment = new ProductFragment();
		productFragment.setArguments(bundle);
		transaction.replace(R.id.fragment_container, productFragment,
				"prodotto").setTransition(
				android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.addToBackStack("scan");
		transaction.commit();

	}

	private void interrogaServer(String scanFormat, String scanContent) {
		scanBundle.putString("scanContent",
				new Util().normalizzaUpc(scanContent));
		scanBundle.putString("scanFormat", scanFormat);
		AccountManager mAccountManager = AccountManager.get(getBaseContext());
		Account[] accounts = mAccountManager.getAccounts();
		String token = null;
		EximiaAuthenticator eauth = new EximiaAuthenticator(getBaseContext());
		scanContent4postlogin = scanContent;
		// TODO aggiungere nel bundle per onSaveInstance
		Account laMiaSpesa = getEximiaAccount(accounts);
		// eauth.getAuthToken(response, account, authTokenType, options)
		if (laMiaSpesa != null) { // l'account esiste posso provare a chiuedere
									// il token
			token = mAccountManager.peekAuthToken(laMiaSpesa,
					AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
			String urlRequest = prepareRequest(scanContent, token);
			Log.d(TAG, "interrogo il server");
			Log.d(TAG, urlRequest);
			new GetItemInfo().execute(urlRequest);
			// switch2ProductFragment(scanFormat,scanContent);
		}
	}

	private Account getEximiaAccount(Account[] accounts) {
		Account out = null;
		int i = 0;
		while (i < accounts.length) {
			if (accounts[i].type.equalsIgnoreCase("com.eximia.lamiaspesaapp"))
				out = accounts[i];
			i += 1;
		}
		return out;
	}

	private Account getEximiaAccount() {
		AccountManager mAccountManager = AccountManager.get(getBaseContext());
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

	private String prepareRequest(String scanContent, String token) {
		// Properties props = new Properties();
		Log.d(TAG, "token: " + token);
		StringBuilder url = new StringBuilder(Util.getBaseUrl());
		url.append("/get_item?upc=");
		url.append(scanContent);
		url.append("&token=");
		url.append(token);
		return url.toString();
	}

	private void elaboraRisultatoScan(IntentResult scanningResult) {
		String scanContent = scanningResult.getContents();
		String scanFormat = scanningResult.getFormatName();
		Log.d(TAG, "elaborato il risultato dello scanner: " + scanContent + " "
				+ scanFormat);
		interrogaServer(scanFormat, scanContent);

	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		if (scanningResult != null) {
			elaboraRisultatoScan(scanningResult);
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"No scan data received!", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private class GetItemInfo extends AsyncTask<String, Void, String> {

		protected void onPostExecute(String result) {
			if (result.equalsIgnoreCase("login")) {
				Log.d("eximia", "sembra che debba loggarmi");
				scanBundle.putBoolean("scan2Complete", true);
				new EseguiLogin().execute();

			} else
				scanBundle.putBoolean("scan2Complete", false);
			if (serverRaggiungibile) {
				Log.d("eximia",
						"sono in onPostExecute, cerco di parsare il json: "
								+ result);
				try {
					JSONObject resultObject = new JSONObject(result);
					Log.d(TAG, "ho parsato il json WW");
					Log.d(TAG, "cerco di estrarre il contenuto di data");
					String token = resultObject.getString("token");
					new Util().setToken(token, getAccountManager());
					JSONObject data = new JSONObject(
							resultObject.getString("data"));
					Log.d(TAG, "upc_number: " + data.getString("upc_number"));
					switch2ProductFragment(data);

				} catch (Exception e) {
					Log.d(TAG, "errore parsando il json ");
					Log.e(TAG, e.toString());
					preparaBundleNoResult();
				}
			} else {

				Toast toast = Toast.makeText(getApplicationContext(),
						"server non raggiungibile!!", Toast.LENGTH_LONG);
				toast.show();
			}
		}

		private AccountManager getAccountManager() {
			AccountManager mAccountManager = AccountManager
					.get(getBaseContext());
			return mAccountManager;
		}

		@Override
		protected String doInBackground(String... productURLs) {
			StringBuilder itemBuilder = new StringBuilder();
			for (String itemSearchURL : productURLs) {
				HttpClient itemClient = new DefaultHttpClient();
				HttpResponse itemResponse = null;
				HttpGet itemGet = null;
				try {
					itemGet = new HttpGet(itemSearchURL);
					Log.d("eximia", itemSearchURL);
					itemResponse = itemClient.execute(itemGet);
					StatusLine itemSearchStatus = itemResponse.getStatusLine();
					if (itemSearchStatus.getStatusCode() == 401) {
						return "login";
					}
					if (itemSearchStatus.getStatusCode() == 200) {
						// we have a result
						HttpEntity itemEntity = itemResponse.getEntity();
						InputStream productContent = itemEntity.getContent();
						InputStreamReader itemInput = new InputStreamReader(
								productContent);
						BufferedReader itemReader = new BufferedReader(
								itemInput);
						String lineIn;
						while ((lineIn = itemReader.readLine()) != null) {
							itemBuilder.append(lineIn);
						}
					}
				} catch (HttpHostConnectException e) {
					Log.d(TAG, " server non raggiungibile");
					serverRaggiungibile = false;
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return itemBuilder.toString();
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.scan_button) {
			Log.d(TAG, "richiesto scan");
			IntentIntegrator scanIntegrator = new IntentIntegrator(this);
			AccountManager mAccountManager = AccountManager
					.get(getBaseContext());
			/*
			 * Bundle data = new Bundle();
			 * data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
			 * data.putString(AccountManager.KEY_ACCOUNT_TYPE,
			 * "com.eximia.lamiaspesaapp"); data.putString(PARAM_USER_PASS,
			 * userPass); final Intent intent = new Intent();
			 * intent.putExtras(data); String accountName =
			 * intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME); final
			 * Account account = new Account(accountName,
			 * intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
			 */

			// String token = mAccountManager.peekAuthToken(account,
			// AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
			Log.d(TAG, "ricevuti account");
			if (sviluppo) {
				Log.d(TAG, "modalità sviluppo");
				interrogaServer("UPC_A", "7313468675004");
			} else {
				Log.d("eximia", "modalità produzione");
				scanIntegrator.initiateScan();
			}
		}
	}

	private ImageButton scanBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		scanBundle = SingleBundle.getInstance();
		// TODO settare le view per fragments
		scanBtn = (ImageButton) findViewById(R.id.scan_button);
		ScanFragment scanFragment = new ScanFragment();
		// Create a new Fragment to be placed in the activity layout
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		android.support.v4.app.FragmentTransaction transaction;
		transaction = fm.beginTransaction();
		android.support.v4.app.FragmentTransaction add = transaction.add(
				R.id.fragment_container, scanFragment);
		add.commit();
		// scanBtn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onFragmentInteraction() {
		IntentIntegrator scanIntegrator = new IntentIntegrator(this);
		if (sviluppo)
			interrogaServer("UPC_A", "7313468675004");
		else
			scanIntegrator.initiateScan();
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}

	class EseguiLogin extends AsyncTask<String, Void, String> {

		private String url;
		private Account lamiaspesaapp;
		private AccountManager mAccountManager;

		@Override
		protected void onPreExecute() {
			lamiaspesaapp = getEximiaAccount();
			mAccountManager = AccountManager.get(getBaseContext());
			url = buildUrl();
		}

		private String buildUrl() {
			// TODO Auto-generated method stub
			StringBuilder out = new StringBuilder(Util.getBaseUrl());
			out.append("/api_authentication");
			out.append("?email=");
			out.append(lamiaspesaapp.name);
			out.append("&password=");
			out.append(mAccountManager.getPassword(lamiaspesaapp));
			return out.toString();
		}

		@Override
		protected void onPostExecute(String token) {
			if (token != null)
				mAccountManager.setAuthToken(lamiaspesaapp,
						AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, token);
			if (scanBundle.getBoolean("scan2Complete")) {
				interrogaServer(scanBundle.getString("scanContent"),
						scanBundle.getString("scanFormat"));
			}
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
}
