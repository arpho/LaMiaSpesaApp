package com.eximia.lamiaspesaapp.authentication;

import java.io.FileDescriptor;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class LoginService extends Service {
	private String token = "";
	private Boolean readyToken = false;

	class EseguiLogin extends BundledLogin {
		// extends AsyncTask<String, Void, String>
		private String url;
		private Bundle bundle;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Bundle getBundle() {
			return bundle;
		}

		public void setBundle(Bundle bundle) {
			this.bundle = bundle;
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

		// private Account lamiaspesaapp;
		private AccountManager mAccountManager;

		@Override
		protected void onPreExecute() {
			super.setLamiaspesaapp(getEximiaAccount());
			super.setmAccountManager(AccountManager.get(getBaseContext()));
			/*
			 * super.setUrl(super.buildUrl(bundle)); url = buildUrl(bundle);
			 */
		}

		@Override
		protected void onPostExecute(Bundle bundle) {
			if (bundle != null)
				super.getmAccountManager().setAuthToken(
						super.getLamiaspesaapp(),
						AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
						bundle.getString("token"));
			Log.d("eximia", "ho rinnovato il token");
			readyToken = true;
			/*
			 * if (scanBundle.getBoolean("scan2Complete")) {
			 * interrogaServer(scanBundle.getString("scanContent"),
			 * scanBundle.getString("scanFormat")); }
			 */
		}

	}

	// class MyBinder

	public Boolean isReadyToken() {
		return readyToken;
	}

	public void rinnovaToken(Bundle bundle) {
		EseguiLogin login = new EseguiLogin();
		login.setBundle(bundle);
		login.execute(bundle);

	}

	public String getToken() {
		return token;
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

	private final IBinder myBinder = new MyLocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return myBinder;
	}

	public class MyLocalBinder extends Binder {
		public LoginService getService() {
			Log.d("eximia", "ritorn il servizio");
			return LoginService.this;
		}
	}

}
