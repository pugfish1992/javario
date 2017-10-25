package com.pugfish1992.javario.example;

import com.pugfish1992.javario.annotation.FieldOption;
import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.annotation.ModelSchemaOption;

/**
 * Created by daichi on 10/25/17.
 */

@ModelSchema("Mario")
public class MarioSchema {
    final String shout = "yahoo!";
    final boolean withLuigi = false;
    final long killCount = 50L;
    int lifeCount;
}
