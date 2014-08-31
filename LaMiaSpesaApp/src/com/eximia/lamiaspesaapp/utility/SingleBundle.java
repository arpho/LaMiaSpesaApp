package com.eximia.lamiaspesaapp.utility;

import android.os.Bundle;

public class SingleBundle {

	private static Bundle bundle;

	private SingleBundle() {

	}

	public static Bundle getInstance() {
		if (bundle == null)
			bundle = new Bundle();
		return bundle;
	}

}
