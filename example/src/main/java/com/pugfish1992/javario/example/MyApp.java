package com.pugfish1992.javario.example;

import android.app.Application;

import com.pugfish1992.javario.Javario;
import com.pugfish1992.javario.example.datasource.SQLiteDataSource;

/**
 * Created by daichi on 10/29/17.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SQLiteDataSource sqLiteDataSource = new SQLiteDataSource(this);
        Javario.initialize(sqLiteDataSource);
    }
}
