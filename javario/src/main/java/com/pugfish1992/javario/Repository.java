package com.pugfish1992.javario;

import com.pugfish1992.javario.datasource.DataSource;

import java.util.List;

/**
 * TODO; SUPPORT REMOTE DATA SOURCE.
 */

final class Repository {
    
    private static Repository INSTANCE = null;
    private final DataSource<BaseModel> mLocalDataSource;
    private final ModelCache mCache;

    static void initialize(DataSource<BaseModel> local) {
        INSTANCE = new Repository(local);
    }

    private Repository(DataSource<BaseModel> local) {
        mLocalDataSource = local;
        mCache = new ModelCache();
    }

    static Repository api() {
        return INSTANCE;
    }

    /**
     * CRUD
     * ---------- */

    @SuppressWarnings("unchecked")
    <T extends BaseModel> T findItemFrom(long id, Class<? extends BaseModel> klass) {
        // Respond immediately with cache if available
        T model = mCache.getIfCached(id, klass);
        if (model != null) return model;

        // We need to fetch new data from the local storage
        if (hasLocalSource()) {
            model = (T) mLocalDataSource.findItemFrom(id, klass);
            if (model != null) {
                mCache.cache(model);
                return model;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    <T extends BaseModel> List<T> listItemsFrom(Class<? extends BaseModel> klass) {
        if (hasLocalSource()) {
            List<T> modelList = (List<T>) mLocalDataSource.listItemsFrom(klass);
            if (modelList != null) {
                mCache.clearOf(klass);
                mCache.cacheAll(modelList);
            }
        }

        return null;
    }

    boolean saveItemTo(BaseModel item) {
        if (hasLocalSource()) {
            boolean wasSuccessful = mLocalDataSource.saveItemTo(item);
            if (wasSuccessful) {
                mCache.cache(item);
            }
        }

        return false;
    }

    boolean deleteItemFrom(BaseModel item) {
        mCache.remove(item.getPrimaryKey(), item.getClass());
        if (hasLocalSource()) {
            return mLocalDataSource.deleteItemFrom(item);
        }

        return false;
    }

    /**
     * UTILITY
     * ---------- */

    private boolean hasLocalSource() {
        return mLocalDataSource != null;
    }
}
