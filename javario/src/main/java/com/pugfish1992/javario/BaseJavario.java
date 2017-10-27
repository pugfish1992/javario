package com.pugfish1992.javario;

import com.pugfish1992.javario.datasource.DataSource;

import static com.pugfish1992.javario.ModelGenerationHistoryClassSpec.CLASS_NAME;
import static com.pugfish1992.javario.ModelGenerationHistoryClassSpec.GENERATED_CLASS_PACKAGE;
import static com.pugfish1992.javario.ModelGenerationHistoryClassSpec.METHOD_GET_LIST_OF_SCHEMA_INFO;

import java.util.Collections;
import java.util.List;

/**
 * Created by daichi on 10/26/17.
 */

public final class BaseJavario {

    @SuppressWarnings("unchecked")
    public static void initialize(DataSource<BaseModel> localStorage) {
        Class<?> historyClass;
        try {
            historyClass = Class.forName(GENERATED_CLASS_PACKAGE + "." + CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(CLASS_NAME + " class is not generated");
        }

        List<SchemaInfo> schemaInfoList;
        try {
            schemaInfoList = (List<SchemaInfo>) historyClass.getMethod(METHOD_GET_LIST_OF_SCHEMA_INFO).invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException(METHOD_GET_LIST_OF_SCHEMA_INFO + " method is not generated");
        }

        // Initialize the repository and data sources
        if (localStorage != null) {
            localStorage.onInitialize(Collections.unmodifiableList(schemaInfoList));
        }
        Repository.initialize(localStorage);
    }
}
