package com.pugfish1992.javario;

import com.pugfish1992.javario.datasource.DataSource;

import java.util.List;

final class Repository {
    
    private static Repository INSTANCE = null;
    private final DataSource<BaseModel> mRootDataSource;
    private final ModelCache mCache;

    static void initialize(DataSource<BaseModel> rootDataSource) {
        INSTANCE = new Repository(rootDataSource);
    }

    private Repository(DataSource<BaseModel> rootDataSource) {
        mRootDataSource = rootDataSource;
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

        if (hasLocalSource()) {
            model = (T) mRootDataSource.findItemFrom(id, klass);
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
            List<T> modelList = (List<T>) mRootDataSource.listItemsFrom(klass);
            if (modelList != null) {
                mCache.clearOf(klass);
                mCache.cacheAll(modelList);
                return modelList;
            }
        }

        return null;
    }

    boolean saveItem(BaseModel item) {
        if (hasLocalSource()) {
            boolean wasSuccessful = mRootDataSource.saveItem(item);
            if (wasSuccessful) {
                mCache.cache(item);
                return true;
            }
        }

        return false;
    }

    boolean deleteItem(BaseModel item) {
        mCache.remove(item.getPrimaryKey(), item.getClass());
        if (hasLocalSource()) {
            return mRootDataSource.deleteItem(item);
        }

        return false;
    }

    /**
     * UTILITY
     * ---------- */

    private boolean hasLocalSource() {
        return mRootDataSource != null;
    }
}
