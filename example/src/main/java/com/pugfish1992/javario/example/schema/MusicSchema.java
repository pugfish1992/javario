package com.pugfish1992.javario.example.schema;

import android.view.View;

import com.pugfish1992.javario.annotation.FieldOption;
import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.annotation.ModelSchemaOption;
import com.pugfish1992.javario.annotation.PrimaryKey;

/**
 * Created by daichi on 10/25/17.
 */

@ModelSchema("music")
public class MusicSchema {
    @PrimaryKey
    long id;
    String name;
    String note;
}
