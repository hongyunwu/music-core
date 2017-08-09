package com.autoio.core_sdk.model;

/**
 * Created by wuhongyun on 17-7-18.
 */

public class PlayState {
    /**
     * 空闲状态
     */
    public static final int STATE_IDLE = 0;
    /**
     * 当调用mediaplayer.prepare时
     */
    public static final int STATE_PREPARING = 1;
    /**
     * 当前正处于播放状态
     */
    public static final int STATE_PLAYING = 2;
    /**
     * 当前处于暂停状态
     */
    public static final int STATE_PAUSE = 3;


}
