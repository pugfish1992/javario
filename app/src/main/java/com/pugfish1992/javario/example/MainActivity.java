package com.pugfish1992.javario.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pugfish1992.javario.Javario;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Javario.initialize(null);
    }
}
