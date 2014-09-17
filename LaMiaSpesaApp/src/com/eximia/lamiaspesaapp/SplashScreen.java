package com.eximia.lamiaspesaapp;

import com.eximia.lamiaspesaapp.authentication.LoginService;
import com.eximia.lamiaspesaapp.authentication.LoginService.MyLocalBinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class SplashScreen extends Activity {
	LoginService loginService;
	boolean isBound = false;
	private Account lamiaspesaapp;
	private AccountManager mAccountManager;

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
		Log.d("eximia","cerco account");
		Account out = null;
		int i = 0;
		while (i < accounts.length) {
			if (accounts[i].type.equalsIgnoreCase("com.eximia.lamiaspesaapp"))
				out = accounts[i];
			i += 1;
		}
		return out;
	}

	private ServiceConnection myConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("eximia", "connesso al servizio");
			MyLocalBinder binder = (MyLocalBinder) service;
			loginService = binder.getService();
			isBound = true;
			Bundle bundle = new Bundle();
			bundle.putString("email", lamiaspesaapp.name);
			bundle.putString("password",
					mAccountManager.getPassword(lamiaspesaapp));
			loginService.rinnovaToken(bundle);
			Log.d("eximia", " richiesto token");
			while (!loginService.isReadyToken()) {
				Log.d("eximia", " atteso token");
			}
			Log.d("eximia", "token updated");
		}

		public void onServiceDisconnected(ComponentName arg0) {
			isBound = false;
		}

	};

	private static final int SPLASH_TIME = 3 * 1000;// 3 seconds

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		lamiaspesaapp = getEximiaAccount();
		mAccountManager = AccountManager.get(getBaseContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen);
		Intent intent = new Intent(this, LoginService.class);
		/*getApplicationContext().*/bindService(intent, myConnection, Context.BIND_AUTO_CREATE);

		// TODO rimuovere handler e sostituire con codice di EseguiLogin in
		// ScanActivity
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Log.d("eximia", "lanciato handler");

				Intent intent = new Intent(SplashScreen.this,
						ScanActivity.class);
				startActivity(intent);

				SplashScreen.this.finish();

				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

			}
		}, SPLASH_TIME);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
			}
		}, SPLASH_TIME);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try{
		unbindService(myConnection);
		}finally{
			//TODO da sistemare fa schifo così
		}
	}

	@Override
	public void onBackPressed() {
		this.finish();
		super.onBackPressed();
	}
}
