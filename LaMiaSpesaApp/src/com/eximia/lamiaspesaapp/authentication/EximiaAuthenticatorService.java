package com.eximia.lamiaspesaapp.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created with IntelliJ IDEA.
 * User: Udini
 * Date: 19/03/13
 * Time: 19:10
 */
public class EximiaAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        EximiaAuthenticator authenticator = new EximiaAuthenticator(this);
        return authenticator.getIBinder();
    }
}
