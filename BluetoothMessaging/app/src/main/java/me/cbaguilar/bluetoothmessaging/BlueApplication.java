package me.cbaguilar.bluetoothmessaging;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

public class BlueApplication extends Application {

    public void onCreate() {
        super.onCreate();
        Log.i("Testing!!","testing!!!");
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
    }
}