package com.pugfish1992.javario.example;

import android.view.View;

import com.pugfish1992.javario.annotation.FieldOption;
import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.annotation.ModelSchemaOption;

/**
 * Created by daichi on 10/25/17.
 */

@ModelSchema(value = "Mario")
public class MarioSchema {
    String shout;
    boolean withLuigi;
    long killCount;
    int lifeCount;
}
