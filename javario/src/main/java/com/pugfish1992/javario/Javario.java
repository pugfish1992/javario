package com.pugfish1992.javario;

import com.pugfish1992.javario.datasource.DataSource;

/**
 * Created by daichi on 10/26/17.
 */

public final class Javario {

    public static void initialize(DataSource<BaseModel> localStrage) {
        Repository.initialize(localStrage);
    }
}
