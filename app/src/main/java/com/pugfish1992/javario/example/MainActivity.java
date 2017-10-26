package com.pugfish1992.javario.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.pugfish1992.javario.FieldType;
import com.pugfish1992.javario.Mario;
import com.pugfish1992.javario.ModelUtils;
import com.pugfish1992.javario.annotation.ModelSchema;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
