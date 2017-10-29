package com.pugfish1992.javario.example.schema;

import android.view.View;

import com.pugfish1992.javario.annotation.FieldOption;
import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.annotation.ModelSchemaOption;
import com.pugfish1992.javario.annotation.PrimaryKey;
import com.pugfish1992.javario.datasource.DataSource;

/**
 * Created by daichi on 10/25/17.
 */

@ModelSchema("music")
public class MusicSchema {
    @PrimaryKey
    final long id = DataSource.INVALID_ID;
    String name;
    String note;
    final int rating = 0;
}
