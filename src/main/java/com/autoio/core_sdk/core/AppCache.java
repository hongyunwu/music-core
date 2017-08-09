package com.autoio.core_sdk.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.autoio.core_sdk.model.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzwangchenyan on 2016/11/23.
 */
public class AppCache {
    private Context mContext;
    private PlayService mPlayService;

    // 本地歌曲列表
    private List<Music> mMusicList = new ArrayList<>();

    private List<Music> mLocaleList = new ArrayList<>();

    //我最喜欢的歌曲列表
    private List<Music> mFavoriteList = new ArrayList<>();

    //USB音乐列表
    private List<Music> mUsbList = new ArrayList<>();

    // 歌单列表
    private final List<Activity> mActivityStack = new ArrayList<>();
    private final LongSparseArray<String> mDownloadList = new LongSparseArray<>();



    //USB路径
    private String mUsbPath = "/storage/emulated/0/song";

    private AppCache() {
    }

	/**
     * 获取USB的路径
     * @return
     */
    public static String getUsbPath() {
        return getInstance().mUsbPath;
    }

	/**
	 * 设置USB的路径
     * @param usbPath
     */
    public static void setUsbPath(String usbPath) {
        getInstance().mUsbPath = usbPath;
    }

    private static class SingletonHolder {
        private static AppCache sAppCache = new AppCache();
    }

    private static AppCache getInstance() {
        return SingletonHolder.sAppCache;
    }

    public static void init(Application application) {
        getInstance().onInit(application);
    }

    private void onInit(Application application) {
        mContext = application.getApplicationContext();
        application.registerActivityLifecycleCallbacks(new ActivityLifecycle());
    }

    public static Context getContext() {
        return getInstance().mContext;
    }

    public static PlayService getPlayService() {
        return getInstance().mPlayService;
    }

    public static void setPlayService(PlayService service) {
        getInstance().mPlayService = service;
    }


    public static List<Music> getMusicList() {
        return getInstance().mMusicList;
    }


    public static LongSparseArray<String> getDownloadList() {
        return getInstance().mDownloadList;
    }

    /**
     * 获取本地音乐列表
     * @return
     */
    public static List<Music> getLocaleMusicList(){
        return getInstance().mLocaleList;
    }

    /**
     * 获取我最喜欢音乐列表
     * @return
     */
    public static List<Music> getFavoriteMusicList(){
        return getInstance().mFavoriteList;
    }

    /**
     * 获取usb音乐列表
     * @return
     */
    public static List<Music> getUSBMusicList(){
        return getInstance().mUsbList;
    }

    /**
     * 设置本地音乐列表
     * @param localeList
     */
    public static void setLocaleMusicList(List<Music> localeList){
        getInstance().mLocaleList.clear();
        if (localeList!=null&&localeList.size()>0){
            getInstance().mLocaleList.addAll(localeList);
        }

    }

    /**
     * 添加我最喜欢音乐列表
     * @param favoriteList
     */
    public static void setFavoriteMusicList(List<Music> favoriteList){
        getInstance().mFavoriteList.clear();
        if (favoriteList!=null&&favoriteList.size()>0){
            getInstance().mFavoriteList.addAll(favoriteList);
        }

    }

    /**
     * 添加usb音乐列表
     * @param usbList
     */
    public static void setUSBMusicList(List<Music> usbList){
        getInstance().mUsbList.clear();
        if (usbList!=null&&usbList.size()>0){
            getInstance().mUsbList.addAll(usbList);
        }
    }

    private static class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
        private static final String TAG = "Activity";

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i(TAG, "onCreate: " + activity.getClass().getSimpleName());
            getInstance().mActivityStack.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.i(TAG, "onDestroy: " + activity.getClass().getSimpleName());
        }
    }
}
