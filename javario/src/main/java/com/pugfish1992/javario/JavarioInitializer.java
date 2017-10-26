package com.pugfish1992.javario;

import com.pugfish1992.javario.datasource.DataSource;

/**
 * Created by daichi on 10/26/17.
 */

public final class JavarioInitializer {

    private DataSource<BaseModel> mLocalDataSource;

    public static JavarioInitializer begin() {
        return new JavarioInitializer();
    }

    public JavarioInitializer localStrage(DataSource<BaseModel> local) {
        mLocalDataSource = local;
        return this;
    }

    public void initialize() {
        Repository.initialize(mLocalDataSource);
    }
}
