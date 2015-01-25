package com.gmail.dajinchu.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gmail.dajinchu.MainGame;
import com.splunk.mint.Mint;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Mint.initAndStartSession(AndroidLauncher.this, "e777a26b");
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new MainGame(new LANConnect(this)), config);
	}
}
