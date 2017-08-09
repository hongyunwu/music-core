package com.autoio.core_sdk.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.autoio.core_sdk.model.Music;
import com.autoio.core_sdk.model.PlayAction;
import com.autoio.core_sdk.model.PlayMode;
import com.autoio.core_sdk.model.PlayState;
import com.autoio.core_sdk.receiver.NoisyAudioStreamReceiver;
import com.autoio.core_sdk.utils.MusicUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by wuhongyun on 17-7-18.
 *
 * 后台服务，用于控制管理音乐播放逻辑
 */
public class PlayService extends Service implements AudioManager.OnAudioFocusChangeListener, IMediaPlayer.OnCompletionListener {
    /**
     * 装music的集合
     */
    private List<Music> mMusicList = null;

    /**
     * 当前的播放模式,默认处于列表循环
     */
    private int mPlayMode = PlayMode.MODE_REPEAT;

    /**
     * 当前的播放状态，默认处于空闲状态
     */
    private int mPlayState = PlayState.STATE_IDLE;

    /**
     * 播放器
     */
    private IjkMediaPlayer mMediaPlayer = new IjkMediaPlayer();

    private IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private NoisyAudioStreamReceiver mNoisyReceiver = new NoisyAudioStreamReceiver();
    private AudioManager mAudioManager;

    /**
     * 正在播放的位置
     */
    private int mPlayingPosition;

    /**
     * 当前正在播放的歌曲
     */
    private Music mPlayingMusic;

    /**
     * 播放的回调监听
     */
    private List<OnPlayerEventListener> mListeners = new ArrayList<>();
    /**
     * 此handler控制时间的更新
     */
    private Handler mHandler = new Handler();
    /**
     * 时间更新间隔
     */
    private static final long TIME_UPDATE = 100L;
    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        /*AppCache.setPlayService(this);*/

        mMediaPlayer.setOnCompletionListener(this);
        Log.i("PlayService","onCreate...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return new PlayBinder();
    }

    /**
     * 扫描sd卡的音乐，默认是在主线程中
     */
    public void scanMusic(){
        clearMusic();
        //TODO 扫描之前先判断是否允许读取sd卡
        MusicUtils.scanMusic(this,mMusicList);


    }

	/**
	 * 获取音乐列表
     * @return
     */
    public List<Music> getMusicList(){
        return mMusicList;
    }

    /**
     * 获取正在播放的歌曲
     *
     */
    public Music getPlayingMusic(){

        return mPlayingMusic;
    }

    /**
     * 清除音乐列表的集合
     */
    public void clearMusic() {
        mMusicList.clear();
    }

    /**
     * 添加歌曲到音乐列表
     */
    public void addMusic(Music music){
        if (music!=null){
            mMusicList.add(music);
        }
    }

    /**
     * 从音乐列表删除歌曲
     * @param music
     */
    public void deleteMusic(Music music){
        if (music!=null){
            mMusicList.remove(music);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {

                case PlayAction.ACTION_PLAY_PAUSE:
                    playPause();
                    break;
                case PlayAction.ACTION_NEXT:
                    next();
                    break;
                case PlayAction.ACTION_PREVIOUS:
                    previous();
                    break;
            }

        }

        return START_NOT_STICKY;
    }

    /**
     * 上一首
     */
    private void previous() {
        if (mMusicList.isEmpty()) {
            return;
        }
        switch (mPlayMode){
            case PlayMode.MODE_RANDOM:
                mPlayingPosition = new Random().nextInt(mMusicList.size());
                play(mPlayingPosition);
                break;
            case PlayMode.MODE_REPEAT:
                play(mPlayingPosition - 1);
                break;
            case PlayMode.MODE_REPEAT_ONCE:
                play(mPlayingPosition);
                break;
        }

    }

    /**
     * 下一首
     */
    private void next() {
        if (mMusicList.isEmpty()) {
            return;
        }
        switch (mPlayMode){
            case PlayMode.MODE_RANDOM:
                mPlayingPosition = new Random().nextInt(mMusicList.size());
                play(mPlayingPosition);
                break;
            case PlayMode.MODE_REPEAT:
                play(mPlayingPosition + 1);
                break;
            case PlayMode.MODE_REPEAT_ONCE:
                play(mPlayingPosition);
                break;
        }
    }

    /**
     * 开始播放,一般为异步
     */
    public void play(int position) {
        if (mMusicList.isEmpty()) {
            return;
        }

        if (position < 0) {
            position = mMusicList.size() - 1;
        } else if (position >= mMusicList.size()) {
            position = 0;
        }

        mPlayingPosition = position;
        Music music = mMusicList.get(mPlayingPosition);
        //TODO 保存正在播放的歌曲信息
        playMusic(music);
        Log.i("播放状态","position:"+position);

    }
    /**
     * 播放歌曲
     * @param music 要播放的歌曲
     */
    public void playMusic(Music music) {
        if (mMusicList.contains(music)){
            mPlayingPosition = mMusicList.indexOf(music);
        }

        mHandler.removeCallbacks(mPublishRunnable);
        mPlayingMusic = music;
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(music.getPath());
            mMediaPlayer.prepareAsync();
            mPlayState = PlayState.STATE_PREPARING;
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            if (mListeners.size()>0) {

                for (OnPlayerEventListener listener : mListeners){
                    listener.onChange(music);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 暂停后播放
     */
    public void resume() {
        if (!isPausing()) {
            return;
        }

        boolean start = start();
        if (start) {

            if (mListeners.size()>0) {

                for (OnPlayerEventListener listener : mListeners){
                    listener.onPlayerResume();
                }

            }
        }
        Log.i("播放状态","resume..."+start);
    }
    /**
     *
     */
    public void playPause(){
        if (isPreparing()) {
            return;
        }
        if (isPlaying()) {
            pause();
        } else if (isPausing()) {
            resume();
        } else {//IDLE
            play(getPlayingPosition());//有可能进入prepare状态
        }

    }
    /**
     * 开始播放
     * @return
     */
    public boolean start() {
        mMediaPlayer.start();
        mPlayState = PlayState.STATE_PLAYING;
        mHandler.post(mPublishRunnable);
        if (mMediaPlayer.isPlaying()) {
            //TODO 开始更新时间
            mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        }
        registerReceiver(mNoisyReceiver, mNoisyFilter);
        return mMediaPlayer.isPlaying();
    }

    /**
     * 跳转到指定的时间位置
     *
     * @param msec 时间
     */
    public void seekTo(int msec) {
        if (isPlaying() || isPausing()) {
            mMediaPlayer.seekTo(msec);

            if (mListeners.size()>0) {

                for (OnPlayerEventListener listener : mListeners){
                    listener.onPublish(msec);
                }

            }

        }
    }

    /**
     * 暂停
     */
    public void pause() {
        if (!isPlaying()){
            return;
        }

        mMediaPlayer.pause();
        mPlayState = PlayState.STATE_PAUSE;
        mAudioManager.abandonAudioFocus(this);
        try {
            unregisterReceiver(mNoisyReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (mListeners.size()>0) {

            for (OnPlayerEventListener listener : mListeners){
                listener.onPlayerPause();
            }
        }
        //TODO 取消更新时间
        mHandler.removeCallbacks(mPublishRunnable);
        Log.i("播放状态","pause...");
    }

    /**
     * 停止服务
     */
    public void stop() {
        pause();

        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
        AppCache.setPlayService(null);
        stopSelf();
    }

    /**
     * 当前正在播放歌曲
     * @return
     */
    public boolean isPlaying() {
        return mPlayState == PlayState.STATE_PLAYING;
    }

    /**
     * 当前正处于暂停状态
     * @return
     */
    public boolean isPausing() {
        return mPlayState == PlayState.STATE_PAUSE;
    }

    /**
     * 获取当前的播放位置
     *
     * @return 当前播放的位置
     */
    public int getPlayingPosition() {
        return mPlayingPosition;
    }
    /**
     * 当前处于准备播放状态
     * @return
     */
    public boolean isPreparing() {
        return mPlayState == PlayState.STATE_PREPARING;
    }

    public boolean isIdle(){
        return mPlayState == PlayState.STATE_IDLE;
    }

    /**
     * 获取播放状态
     * @return
     */
    public int getPlayState() {
        return mPlayState;
    }

    public int getPlayMode(){
        return mPlayMode;
    }

    public void setPlayMode(int playMode){
        mPlayMode = playMode;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                pause();
                break;
        }
    }

    public List<OnPlayerEventListener> getOnPlayEventListener() {
        return mListeners;
    }

    /**
     * 注册
     * @param listener
     */
    public void registerListener(OnPlayerEventListener listener) {
        mListeners.add(listener);
    }

    /**
     * 反注册
     * @param listener
     */
    public void unRegisterListener(OnPlayerEventListener listener){
        if (listener!=null&&mListeners.contains(listener)){

            mListeners.remove(listener);
        }

    }
    /***********************静态方法**********************/

    /**
     * 控制音乐播放
     *
     * @param context
     * @param action 通过action来控制音乐的mode
     */
    public static void startCommand(Context context,String action){
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(action);
        context.startService(intent);
    }



    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        //播放完成直接播放下一首
        next();
    }

    /**
     * 设置播放音乐列表  此list的地址应该与AppCache中的三个list中的一个一致
     * @param musicList
     */
    public void setMusicList(List<Music> musicList) {
        this.mMusicList = musicList;

    }


    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }


    private Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying() && mListeners.size()>0) {
                for (OnPlayerEventListener listener : mListeners){
                    listener.onPublish(mMediaPlayer.getCurrentPosition());
                }

            }
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };
    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {

            if (mListeners.size()>0) {

                for (OnPlayerEventListener listener : mListeners){
                    listener.onBufferingUpdate(percent);
                }
            }
        }
    };

    private IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            start();
        }
    };
}
