package com.pugfish1992.javario.example;

import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.annotation.PrimaryKey;

/**
 * Created by daichi on 10/27/17.
 */

@ModelSchema("Luigi")
public class LuigiSchema {
    @PrimaryKey
    long luigiId;
    int age;
    String shout;
}
