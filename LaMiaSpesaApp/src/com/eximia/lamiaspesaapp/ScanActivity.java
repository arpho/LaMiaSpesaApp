package com.eximia.lamiaspesaapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.eximia.lamiaspesaapp.utility.Util;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanActivity extends FragmentActivity implements
		View.OnClickListener, ScanFragment.OnFragmentInteractionListener,
		ProductFragment.OnFragmentInteractionListener {
	private static final String TAG = ScanActivity.class.getSimpleName();
	private final Boolean sviluppo = true;
	private Boolean serverRaggiungibile = true;

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
		String urlRequest = prepareRequest(scanContent);
		Log.d(TAG, "interrogo il server");
		new GetItemInfo().execute(urlRequest);
		// switch2ProductFragment(scanFormat,scanContent);
	}

	private String prepareRequest(String scanContent) {
		// Properties props = new Properties();
		StringBuilder url = new StringBuilder(Util.getBaseUrl());
		url.append("/get_item?upc=");
		url.append(scanContent);
		return url.toString();
	}

	private String getBaseUrl4() {
		// TODO deve ottenere l'indirizzo da un file di properties
		return "http://192.168.1.66:8080";
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
			if (serverRaggiungibile) {
				Log.d(TAG, "sono in onPostExecute, cerco di parsare il json: "
						+ result);
				try {
					JSONObject resultObject = new JSONObject(result);
					Log.d(TAG, "ho parsato il json WW");
					Log.d(TAG, "cerco di estrarre ilcontenuto di data");
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

		@Override
		protected String doInBackground(String... productURLs) {
			StringBuilder itemBuilder = new StringBuilder();
			for (String itemSearchURL : productURLs) {
				HttpClient itemClient = new DefaultHttpClient();
				try {
					HttpGet itemGet = new HttpGet(itemSearchURL);
					HttpResponse itemResponse = itemClient.execute(itemGet);
					StatusLine itemSearchStatus = itemResponse.getStatusLine();
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
			IntentIntegrator scanIntegrator = new IntentIntegrator(this);
			if (sviluppo)
				interrogaServer("UPC_A", "7313468675004");
			else
				scanIntegrator.initiateScan();
		}
	}

	private ImageButton scanBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
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
}
