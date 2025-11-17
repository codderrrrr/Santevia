package com.example.medilink;

public class AppCache {
    private static final AppCache INSTANCE = new AppCache();
    private FireBaseDataLoader.LoadedData loadedData;

    private AppCache() {}
    public static AppCache getInstance() { return INSTANCE; }
    public void setLoadedData(FireBaseDataLoader.LoadedData data) { this.loadedData = data; }
    public FireBaseDataLoader.LoadedData getLoadedData() { return loadedData; }
}
