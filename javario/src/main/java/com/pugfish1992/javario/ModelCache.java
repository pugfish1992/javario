package com.pugfish1992.javario;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by daichi on 10/26/17.
 */

final class ModelCache {

    /*

    Cache = {@key = Class object of a Model class, @value = SubCache}
    SubCache = {@key = id of a model object, @value = model object}

     */
    private final HashMap<Class<? extends BaseModel>, HashMap<Long, BaseModel>> mCache;

    ModelCache() {
        mCache = new HashMap<>();
    }

    void cache(BaseModel model) {
        HashMap<Long, BaseModel> subCache = mCache.get(model.getClass());
        if (subCache == null) {
            subCache = new HashMap<>();
            subCache.put(model.getPrimaryKey(), model);
            mCache.put(model.getClass(), subCache);
        } else {
            subCache.put(model.getPrimaryKey(), model);
        }
    }

    void cacheAll(List<? extends BaseModel> models) {
        if (models.size() == 0) return;

        Class<? extends BaseModel> klass = models.get(0).getClass();
        HashMap<Long, BaseModel> subCache = mCache.get(klass);
        if (subCache == null) {
            subCache = new HashMap<>();
            mCache.put(klass, subCache);
        }

        for (BaseModel model : models) {
            subCache.put(model.getPrimaryKey(), model);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends BaseModel> T getIfCached(long id, Class<? extends BaseModel> klass) {
        HashMap<Long, BaseModel> subCache = mCache.get(klass);
        if (subCache == null) return null;
        return (T) subCache.get(id);
    }

    BaseModel remove(long id, Class<? extends BaseModel> klass) {
        HashMap<Long, BaseModel> subCache = mCache.get(klass);
        if (subCache == null) return null;
        return subCache.remove(id);
    }

    void clearAll() {
        mCache.clear();
    }

    void clearOf(Class<? extends BaseModel> klass) {
        HashMap<Long, BaseModel> subCache = mCache.get(klass);
        if (subCache != null) {
            subCache.clear();
        }
    }
}
